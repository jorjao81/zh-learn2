package com.zhlearn.cli;

import com.zhlearn.application.audio.AudioOrchestrator;
import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;
import com.zhlearn.application.service.AnkiExporter;
import com.zhlearn.application.service.ParallelWordAnalysisService;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.domain.service.WordAnalysisService;
import com.zhlearn.cli.audio.InteractiveAudioUI;
import com.zhlearn.cli.audio.PrePlayback;
import com.zhlearn.cli.audio.SystemAudioPlayer;
import com.zhlearn.infrastructure.pleco.PlecoEntry;
import com.zhlearn.infrastructure.pleco.PlecoExportParser;
import com.zhlearn.infrastructure.dictionary.PlecoExportDictionary;
import com.zhlearn.infrastructure.dictionary.DictionaryDefinitionProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryPinyinProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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
    
    @Option(names = {"--pinyin-provider"}, description = "Set specific provider for pinyin (default: pleco-export). Available: pinyin4j, dummy, pleco-export", defaultValue = "pleco-export")
    private String pinyinProvider;
    
    @Option(names = {"--definition-provider"}, description = "Set specific provider for definition (default: pleco-export). Available: dummy, pleco-export", defaultValue = "pleco-export")
    private String definitionProvider;
    
    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition (default: deepseek-chat). Available: dummy, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5", defaultValue = "deepseek-chat")
    private String decompositionProvider;

    @Option(names = {"--example-provider"}, description = "Set specific provider for examples (default: deepseek-chat). Available: dummy, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5", defaultValue = "deepseek-chat")
    private String exampleProvider;

    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation (default: deepseek-chat). Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5", defaultValue = "deepseek-chat")
    private String explanationProvider;
    
    @Option(names = {"--audio-provider"}, description = "Set specific provider for audio pronunciation (default: anki). Available: anki, forvo, qwen-tts", defaultValue = "anki")
    private String audioProvider;
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;
    
    @Option(names = {"--limit"}, description = "Limit the number of words to process (default: all)")
    private Integer limit;
    
    @Option(names = {"--parallel-threads"}, description = "Number of parallel threads for processing (default: 10)", defaultValue = "10")
    private int parallelThreads;
    
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

            // Note: Dictionary providers are no longer dynamically registered
            // They are created at startup in MainCommand

            // Set up word analysis service (parallel or sequential)
            WordAnalysisService wordAnalysisService;
            ParallelWordAnalysisService parallelService = null;

            // Create providers with special handling for pleco-export which needs the dictionary
            ExampleProvider exampleProv = parent.createExampleProvider(exampleProvider);
            ExplanationProvider explanationProv = parent.createExplanationProvider(explanationProvider);
            StructuralDecompositionProvider decompositionProv = parent.createDecompositionProvider(decompositionProvider);
            PinyinProvider pinyinProv = "pleco-export".equals(pinyinProvider) ? new DictionaryPinyinProvider(dictionary) : parent.createPinyinProvider(pinyinProvider);
            DefinitionProvider definitionProv = "pleco-export".equals(definitionProvider) ? new DictionaryDefinitionProvider(dictionary) : parent.createDefinitionProvider(definitionProvider);
            AudioProvider audioProv = resolveAudioProvider(audioProvider);

            WordAnalysisServiceImpl baseService = new WordAnalysisServiceImpl(
                exampleProv, explanationProv, decompositionProv, pinyinProv, definitionProv, audioProv
            );

            if (disableParallelism) {
                wordAnalysisService = baseService;
                System.out.println("Using sequential processing (parallelism disabled)");
            } else {
                parallelService = new ParallelWordAnalysisService(baseService, parallelThreads);
                wordAnalysisService = parallelService;
                System.out.println("Using parallel processing with " + parallelThreads + " threads");
            }

            ProviderConfiguration config = new ProviderConfiguration(
                exampleProvider,
                pinyinProvider,
                definitionProvider,
                decompositionProvider,
                exampleProvider,
                explanationProvider,
                audioProvider
            );

            // Validate all providers before processing
            // Provider validation is no longer needed since providers are fixed
            
            // Process words through the analysis pipeline
            int maxToProcess = limit != null ? limit : entries.size();
            List<PlecoEntry> entriesToProcess = entries.stream()
                .limit(maxToProcess)
                .collect(Collectors.toList());
            
            if (!entriesToProcess.isEmpty()) {
                ensureInteractiveAudioSupported();
            }

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
            
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse Pleco export at " + filePath, e);
        }
    }
    
    private void processWordsSequentially(List<PlecoEntry> entries, WordAnalysisService wordAnalysisService,
                                        ProviderConfiguration config, int maxToProcess, List<WordAnalysis> successfulAnalyses) {
        int processedCount = 0;
        AudioOrchestrator audioOrchestrator = new AudioOrchestrator(parent.getAudioProviders());
        InteractiveAudioUI audioUI = new InteractiveAudioUI();

        for (PlecoEntry entry : entries) {
            try {
                Hanzi word = new Hanzi(entry.hanzi());
                WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
                printWordAnalysis(analysis, processedCount + 1, maxToProcess);
                WordAnalysis updated = runAudioSelection(audioOrchestrator, audioUI, analysis);
                successfulAnalyses.add(updated); // Collect for export
                processedCount++;
            } catch (RuntimeException e) {
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
                    } catch (RuntimeException e) {
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

            if (!successfulAnalyses.isEmpty()) {
                AudioOrchestrator audioOrchestrator = new AudioOrchestrator(parent.getAudioProviders());
                InteractiveAudioUI audioUI = new InteractiveAudioUI();
                for (int i = 0; i < successfulAnalyses.size(); i++) {
                    WordAnalysis updated = runAudioSelection(audioOrchestrator, audioUI, successfulAnalyses.get(i));
                    successfulAnalyses.set(i, updated);
                }
            }

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

    private WordAnalysis runAudioSelection(AudioOrchestrator orchestrator, InteractiveAudioUI audioUI, WordAnalysis analysis) {
        List<PronunciationCandidate> rawCandidates = orchestrator.candidatesFor(analysis.word(), analysis.pinyin());
        if (rawCandidates.isEmpty()) {
            System.out.printf("No pronunciation candidates available for '%s'.%n%n", analysis.word().characters());
            return analysis;
        }

        List<PronunciationCandidate> candidates = PrePlayback.preprocessCandidates(analysis.word(), analysis.pinyin(), rawCandidates);
        if (candidates.isEmpty()) {
            System.out.printf("No playable pronunciation candidates available for '%s'.%n%n", analysis.word().characters());
            return analysis;
        }

        System.out.printf("Selecting audio for '%s' (%s)%n", analysis.word().characters(), analysis.pinyin().pinyin());
        SystemAudioPlayer player = new SystemAudioPlayer();
        SelectionSession session = new SelectionSession(candidates, player);
        PronunciationCandidate choice;
        try {
            choice = audioUI.run(session);
        } finally {
            player.stop();
        }

        if (choice == null) {
            System.out.println("No audio selected.");
            System.out.println();
            return analysis;
        }

        System.out.println("Selected audio: " + choice.file().toAbsolutePath());
        System.out.println();

        return new WordAnalysis(
            analysis.word(),
            analysis.pinyin(),
            analysis.definition(),
            analysis.structuralDecomposition(),
            analysis.examples(),
            analysis.explanation(),
            Optional.of(choice.file().toAbsolutePath())
        );
    }

    private void ensureInteractiveAudioSupported() {
        if (System.console() != null) {
            return;
        }

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            if (terminal == null || "dumb".equalsIgnoreCase(terminal.getType())) {
                failNonInteractiveTerminal();
            }
        } catch (IOException e) {
            System.err.println("Unable to initialize terminal for audio selection: " + e.getMessage());
            System.exit(1);
        } catch (IllegalStateException e) {
            failNonInteractiveTerminal();
        }
    }

    private void failNonInteractiveTerminal() {
        System.err.println("parse-pleco requires an interactive terminal that supports raw mode for audio selection.");
        System.err.println("Run zh-learn from a terminal (no piping or redirection) to continue.");
        System.exit(1);
    }
    
    /**
     * Helper class to hold analysis results from parallel processing
     */
    private static class WordAnalysisResult {
        final PlecoEntry entry;
        final WordAnalysis analysis;
        final RuntimeException error;
        final long duration; // Duration in milliseconds
        
        WordAnalysisResult(PlecoEntry entry, WordAnalysis analysis, RuntimeException error, long duration) {
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
            
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to export Anki file to " + filename, e);
        }
    }

    // Printing is delegated to AnalysisPrinter to match 'word' command output

    private AudioProvider resolveAudioProvider(String providerName) {
        return parent.getAudioProviders().stream()
            .filter(provider -> provider.getName().equals(providerName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown audio provider: " + providerName));
    }
}
