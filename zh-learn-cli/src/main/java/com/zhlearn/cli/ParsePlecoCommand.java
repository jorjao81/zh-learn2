package com.zhlearn.cli;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.application.service.ParallelWordAnalysisService;
import com.zhlearn.application.service.AnkiExporter;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.service.WordAnalysisService;
import com.zhlearn.infrastructure.dictionary.PlecoExportDictionary;
import com.zhlearn.infrastructure.dictionary.DictionaryDefinitionProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryPinyinProvider;
import com.zhlearn.infrastructure.pleco.PlecoEntry;
import com.zhlearn.infrastructure.pleco.PlecoExportParser;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * CLI command to parse Pleco export files and process all words through the analysis pipeline.
 * Processes each word found in the export like the existing "word" command.
 */
@Command(
    name = "parse-pleco",
    description = "Parse Pleco export file and analyze all Chinese words"
)
public class ParsePlecoCommand implements Runnable {
    
    @Parameters(index = "0", description = "Path to the Pleco export file (TSV format)")
    private String filePath;
    
    @Option(names = {"--provider"}, description = "Set default provider for all services (parse-pleco defaults: pleco-export for definition/pinyin, deepseek-chat for analysis, existing-anki-pronunciation for audio). Available: dummy, pinyin4j, gpt-5-nano, deepseek-chat, pleco-export, existing-anki-pronunciation")
    private String defaultProvider = "dummy";
    
    @Option(names = {"--pinyin-provider"}, description = "Set specific provider for pinyin (default: pleco-export). Available: pinyin4j, dummy, pleco-export")
    private String pinyinProvider;
    
    @Option(names = {"--definition-provider"}, description = "Set specific provider for definition (default: pleco-export). Available: dummy, pleco-export")
    private String definitionProvider;
    
    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition (default: deepseek-chat). Available: dummy, gpt-5-nano, deepseek-chat")
    private String decompositionProvider;
    
    @Option(names = {"--example-provider"}, description = "Set specific provider for examples (default: deepseek-chat). Available: dummy, gpt-5-nano, deepseek-chat")
    private String exampleProvider;
    
    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation (default: deepseek-chat). Available: dummy, gpt-5-nano, deepseek-chat")
    private String explanationProvider;
    
    @Option(names = {"--audio-provider"}, description = "Set specific provider for audio pronunciation (default: existing-anki-pronunciation). Available: existing-anki-pronunciation")
    private String audioProvider;
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;
    
    @Option(names = {"--limit"}, description = "Limit the number of words to process (default: all)")
    private Integer limit;
    
    @Option(names = {"--parallel-threads"}, description = "Number of parallel threads for processing (default: 10)")
    private Integer parallelThreads = 10;
    
    @Option(names = {"--disable-parallelism"}, description = "Disable parallel processing, use sequential processing instead")
    private boolean disableParallelism = false;
    
    @Option(names = {"--export-anki"}, description = "Export results to Anki-compatible TSV file (Chinese 2 format)")
    private String ankiExportFile;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;
    
    @Override
    public void run() {
        try {
            Path path = Paths.get(filePath);
            PlecoExportParser parser = new PlecoExportParser();
            List<PlecoEntry> entries = parser.parseFile(path);
            
            System.out.println("Successfully parsed " + entries.size() + " entries from: " + filePath);
            System.out.println();
            
            // Create dictionary for any dictionary-based providers
            PlecoExportDictionary dictionary = new PlecoExportDictionary(entries);

            // Register dictionary-backed providers so users can opt-in via --*-provider flags
            var registry = parent.getProviderRegistry();
            registry.registerDefinitionProvider(new DictionaryDefinitionProvider(dictionary));
            registry.registerPinyinProvider(new DictionaryPinyinProvider(dictionary));
            
            // Set up word analysis service (parallel or sequential)
            WordAnalysisService wordAnalysisService;
            ParallelWordAnalysisService parallelService = null;
            
            if (disableParallelism) {
                wordAnalysisService = new WordAnalysisServiceImpl(parent.getProviderRegistry());
                System.out.println("Using sequential processing (parallelism disabled)");
            } else {
                parallelService = new ParallelWordAnalysisService(parent.getProviderRegistry(), parallelThreads);
                wordAnalysisService = parallelService;
                System.out.println("Using parallel processing with " + parallelThreads + " threads");
            }
            
            // Set parse-pleco specific defaults
            String effectiveDefinitionProvider = definitionProvider != null ? definitionProvider : "pleco-export";
            String effectivePinyinProvider = pinyinProvider != null ? pinyinProvider : "pleco-export";
            String effectiveDecompositionProvider = decompositionProvider != null ? decompositionProvider : "deepseek-chat";
            String effectiveExampleProvider = exampleProvider != null ? exampleProvider : "deepseek-chat";
            String effectiveExplanationProvider = explanationProvider != null ? explanationProvider : "deepseek-chat";
            String effectiveAudioProvider = audioProvider != null ? audioProvider : "existing-anki-pronunciation";
            
            ProviderConfiguration config = new ProviderConfiguration(
                defaultProvider,
                effectivePinyinProvider,
                effectiveDefinitionProvider,
                effectiveDecompositionProvider,
                effectiveExampleProvider,
                effectiveExplanationProvider,
                effectiveAudioProvider
            );
            
            // Process words through the analysis pipeline
            int maxToProcess = limit != null ? limit : entries.size();
            List<PlecoEntry> entriesToProcess = entries.stream()
                .limit(maxToProcess)
                .collect(Collectors.toList());
            
            // Thread-safe collection to store successful analyses for export
            List<WordAnalysis> successfulAnalyses = new CopyOnWriteArrayList<>();
            
            if (disableParallelism) {
                // Sequential processing
                processWordsSequentially(entriesToProcess, wordAnalysisService, config, maxToProcess, successfulAnalyses);
            } else {
                // Parallel word-level processing
                processWordsInParallel(entriesToProcess, wordAnalysisService, config, maxToProcess, parallelThreads, successfulAnalyses);
                
                // Shutdown the parallel service
                if (parallelService != null) {
                    parallelService.shutdown();
                }
            }
            
            // Export to Anki file if requested
            if (ankiExportFile != null && !ankiExportFile.trim().isEmpty()) {
                exportToAnkiFile(successfulAnalyses, ankiExportFile.trim());
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing Pleco file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void processWordsSequentially(List<PlecoEntry> entries, WordAnalysisService wordAnalysisService, 
                                        ProviderConfiguration config, int maxToProcess, List<WordAnalysis> successfulAnalyses) {
        int processedCount = 0;
        
        for (PlecoEntry entry : entries) {
            try {
                Hanzi word = new Hanzi(entry.hanzi());
                WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
                
                printWordAnalysis(analysis, processedCount + 1, maxToProcess);
                successfulAnalyses.add(analysis); // Collect for export
                processedCount++;
                
            } catch (Exception e) {
                System.err.println("Error analyzing word '" + entry.hanzi() + "': " + e.getMessage());
            }
        }
        
        System.out.println("Processed " + processedCount + " words successfully.");
    }
    
    private void processWordsInParallel(List<PlecoEntry> entries, WordAnalysisService wordAnalysisService,
                                      ProviderConfiguration config, int maxToProcess, int threadCount, List<WordAnalysis> successfulAnalyses) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // Thread-safe counters for progress tracking
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        try {
            long overallStartTime = System.currentTimeMillis();
            
            // Create futures that display results immediately upon completion
            List<CompletableFuture<Void>> displayFutures = entries.stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> {
                    long wordStartTime = System.currentTimeMillis();
                    try {
                        Hanzi word = new Hanzi(entry.hanzi());
                        WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
                        long wordDuration = System.currentTimeMillis() - wordStartTime;
                        return new WordAnalysisResult(entry, analysis, null, wordDuration);
                    } catch (Exception e) {
                        long wordDuration = System.currentTimeMillis() - wordStartTime;
                        return new WordAnalysisResult(entry, null, e, wordDuration);
                    }
                }, executor)
                .thenAccept(result -> {
                    // Display result immediately when ready
                    synchronized(this) {
                        int completed = completedCount.incrementAndGet();
                        double percentage = (completed * 100.0) / maxToProcess;
                        
                        if (result.analysis != null) {
                            int successIndex = successCount.incrementAndGet();
                            
                            System.out.println("=".repeat(80));
                            System.out.printf("Word %d/%d (%.1f%%) - '%s' (completed in %.2fs)%n", 
                                completed, maxToProcess, percentage, result.entry.hanzi(), result.duration / 1000.0);
                            System.out.println("=".repeat(80));
                            
                            if (rawOutput) {
                                AnalysisPrinter.printRaw(result.analysis);
                            } else {
                                AnalysisPrinter.printFormatted(result.analysis);
                            }
                            
                            System.out.println();
                            
                            // Collect for export
                            successfulAnalyses.add(result.analysis);
                        } else {
                            errorCount.incrementAndGet();
                            System.err.printf("Error analyzing word '%s' (%.2fs): %s%n", 
                                result.entry.hanzi(), result.duration / 1000.0, result.error.getMessage());
                        }
                    }
                }))
                .collect(Collectors.toList());
            
            // Wait for all displays to complete
            CompletableFuture.allOf(displayFutures.toArray(new CompletableFuture[0])).join();
            
            long overallDuration = System.currentTimeMillis() - overallStartTime;
            System.out.println("=".repeat(80));
            System.out.printf("Processing complete! %d words successful, %d errors in %.2fs%n", 
                successCount.get(), errorCount.get(), overallDuration / 1000.0);
            
        } finally {
            executor.shutdown();
        }
    }
    
    private void printWordAnalysis(WordAnalysis analysis, int currentIndex, int total) {
        System.out.println("=".repeat(80));
        System.out.println("Word " + currentIndex + "/" + total);
        System.out.println("=".repeat(80));
        
        if (rawOutput) {
            AnalysisPrinter.printRaw(analysis);
        } else {
            AnalysisPrinter.printFormatted(analysis);
        }
        
        System.out.println();
    }
    
    /**
     * Helper class to hold analysis results from parallel processing
     */
    private static class WordAnalysisResult {
        final PlecoEntry entry;
        final WordAnalysis analysis;
        final Exception error;
        final long duration; // Duration in milliseconds
        
        WordAnalysisResult(PlecoEntry entry, WordAnalysis analysis, Exception error, long duration) {
            this.entry = entry;
            this.analysis = analysis;
            this.error = error;
            this.duration = duration;
        }
    }
    
    /**
     * Export the successful WordAnalysis results to an Anki-compatible TSV file.
     */
    private void exportToAnkiFile(List<WordAnalysis> analyses, String filename) {
        try {
            AnkiExporter exporter = new AnkiExporter();
            exporter.exportToFile(analyses, filename);
            
            System.out.println("=".repeat(80));
            System.out.printf("Exported %d words to %s (Anki Chinese 2 format)%n", analyses.size(), filename);
            
        } catch (Exception e) {
            System.err.println("Error exporting to Anki file: " + e.getMessage());
        }
    }
    
    // Printing is delegated to AnalysisPrinter to match 'word' command output
}
