package com.zhlearn.cli;

import com.zhlearn.application.audio.AudioOrchestrator;
import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;
import com.zhlearn.application.service.AnkiExporter;
import com.zhlearn.application.service.ParallelWordAnalysisService;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;
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

    @Option(names = {"--definition-formatter-provider"}, description = "Set specific provider for definition formatting (default: deepseek-chat). Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter", defaultValue = "deepseek-chat")
    private String definitionFormatterProvider;

    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition (default: deepseek-chat). Available: dummy, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5, openrouter", defaultValue = "deepseek-chat")
    private String decompositionProvider;

    @Option(names = {"--example-provider"}, description = "Set specific provider for examples (default: deepseek-chat). Available: dummy, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5, openrouter", defaultValue = "deepseek-chat")
    private String exampleProvider;

    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation (default: deepseek-chat). Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5, openrouter", defaultValue = "deepseek-chat")
    private String explanationProvider;
    
    @Option(names = {"--audio-provider"}, description = "Set specific provider for audio pronunciation (default: anki). Available: anki, forvo, qwen-tts", defaultValue = "anki")
    private String audioProvider;

    @Option(names = {"--model"}, description = "AI model to use with provider (e.g., for OpenRouter: gpt-4, claude-3-sonnet, llama-2-70b-chat)")
    private String model;

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

    @Option(names = {"--skip-audio"}, description = "Skip interactive audio selection")
    private boolean skipAudio = false;

    @Option(names = {"--audio-selections"}, description = "Pre-configured audio selections (format: word:provider:description;word:provider:description)")
    private String audioSelectionsParam;

    private java.util.Map<String, AudioSelection> audioSelections;

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

            // Parse audio selections if provided
            audioSelections = parseAudioSelections(audioSelectionsParam);
            
            // Create dictionary for any dictionary-based providers
            PlecoExportDictionary dictionary = new PlecoExportDictionary(entries);

            // Note: Dictionary providers are no longer dynamically registered
            // They are created at startup in MainCommand

            // Set up word analysis service (parallel or sequential)
            WordAnalysisService wordAnalysisService;
            ParallelWordAnalysisService parallelService = null;

            // Create providers with special handling for pleco-export which needs the dictionary
            ExampleProvider exampleProv = model != null ? parent.createExampleProvider(exampleProvider, model) : parent.createExampleProvider(exampleProvider);
            ExplanationProvider explanationProv = model != null ? parent.createExplanationProvider(explanationProvider, model) : parent.createExplanationProvider(explanationProvider);
            StructuralDecompositionProvider decompositionProv = model != null ? parent.createDecompositionProvider(decompositionProvider, model) : parent.createDecompositionProvider(decompositionProvider);
            PinyinProvider pinyinProv = "pleco-export".equals(pinyinProvider) ? new DictionaryPinyinProvider(dictionary) : parent.createPinyinProvider(pinyinProvider);
            DefinitionProvider definitionProv = "pleco-export".equals(definitionProvider) ? new DictionaryDefinitionProvider(dictionary) : parent.createDefinitionProvider(definitionProvider);
            DefinitionFormatterProvider defFormatterProv = model != null ? parent.createDefinitionFormatterProvider(definitionFormatterProvider, model) : parent.createDefinitionFormatterProvider(definitionFormatterProvider);
            AudioProvider audioProv = resolveAudioProvider(audioProvider);

            WordAnalysisServiceImpl baseService = new WordAnalysisServiceImpl(
                exampleProv, explanationProv, decompositionProv, pinyinProv, definitionProv, defFormatterProv, audioProv
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
                definitionFormatterProvider,
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
            
            if (!entriesToProcess.isEmpty() && !skipAudio) {
                ensureInteractiveAudioSupported();
            }

            // Thread-safe collection to store successful analyses for export
            List<WordAnalysis> successfulAnalyses = new CopyOnWriteArrayList<>();
            
            if (disableParallelism) {
                // Sequential processing
                processWordsSequentially(entriesToProcess, wordAnalysisService, config, maxToProcess, successfulAnalyses, skipAudio);
            } else {
                // Parallel word-level processing
                processWordsInParallel(entriesToProcess, wordAnalysisService, config, maxToProcess, parallelThreads, successfulAnalyses, skipAudio);
                
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
                                        ProviderConfiguration config, int maxToProcess, List<WordAnalysis> successfulAnalyses, boolean skipAudio) {
        int processedCount = 0;
        AudioOrchestrator audioOrchestrator = skipAudio ? null : new AudioOrchestrator(parent.getAudioProviders(), parent.getAudioExecutor());
        InteractiveAudioUI audioUI = skipAudio ? null : new InteractiveAudioUI();

        for (PlecoEntry entry : entries) {
            Hanzi word = new Hanzi(entry.hanzi());
            WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
            printWordAnalysis(analysis, processedCount + 1, maxToProcess);
            WordAnalysis updated = skipAudio ? analysis : runAudioSelection(audioOrchestrator, audioUI, analysis);
            successfulAnalyses.add(updated); // Collect for export
            processedCount++;
        }

        System.out.println("Processed " + processedCount + " words successfully.");
    }
    
    private void processWordsInParallel(List<PlecoEntry> entries, WordAnalysisService wordAnalysisService,
                                      ProviderConfiguration config, int maxToProcess, int threadCount, List<WordAnalysis> successfulAnalyses, boolean skipAudio) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AudioOrchestrator audioOrchestrator = skipAudio ? null : new AudioOrchestrator(parent.getAudioProviders(), parent.getAudioExecutor());

        // Thread-safe counters for progress tracking
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Thread-safe collection to store successful analysis with audio candidates
        List<WordWithAudioCandidates> wordsWithAudio = new CopyOnWriteArrayList<>();
        
        try {
            long overallStartTime = System.currentTimeMillis();
            
            // Create futures that display results immediately upon completion and generate audio candidates in parallel
            List<CompletableFuture<Void>> displayFutures = entries.stream()
                .map(entry -> {
                    // Launch word analysis and audio candidate generation in parallel
                    CompletableFuture<WordAnalysisResult> analysisFuture = CompletableFuture.supplyAsync(() -> {
                        long wordStartTime = System.currentTimeMillis();
                        Hanzi word = new Hanzi(entry.hanzi());
                        WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
                        long wordDuration = System.currentTimeMillis() - wordStartTime;
                        return new WordAnalysisResult(entry, analysis, wordDuration);
                    }, executor);

                    CompletableFuture<List<PronunciationCandidate>> audioCandidatesFuture = skipAudio
                        ? CompletableFuture.completedFuture(List.of())
                        : CompletableFuture.supplyAsync(() -> {
                            Hanzi word = new Hanzi(entry.hanzi());
                            // We need pinyin for audio candidates, so we get it directly
                            if (wordAnalysisService instanceof ParallelWordAnalysisService parallelService) {
                                // Access the delegate to get pinyin synchronously to avoid double work
                                Pinyin pinyin = parallelService.getPinyin(word, config.getPinyinProvider());
                                System.out.printf("[INFO] Starting audio download for '%s' (%s)%n", word.characters(), pinyin.pinyin());
                                List<PronunciationCandidate> candidates = audioOrchestrator.candidatesFor(word, pinyin);
                                System.out.printf("[INFO] Completed audio download for '%s' - found %d candidates%n", word.characters(), candidates.size());
                                return candidates;
                            } else {
                                // Fallback for non-parallel service
                                Pinyin pinyin = wordAnalysisService.getPinyin(word, config.getPinyinProvider());
                                System.out.printf("[INFO] Starting audio download for '%s' (%s)%n", word.characters(), pinyin.pinyin());
                                List<PronunciationCandidate> candidates = audioOrchestrator.candidatesFor(word, pinyin);
                                System.out.printf("[INFO] Completed audio download for '%s' - found %d candidates%n", word.characters(), candidates.size());
                                return candidates;
                            }
                        }, executor);

                    // Combine both futures and return a CompletableFuture<Void> that processes the display
                    return CompletableFuture.allOf(analysisFuture, audioCandidatesFuture)
                        .thenAccept(ignored -> {
                            try {
                                WordAnalysisResult analysisResult = analysisFuture.get();
                                List<PronunciationCandidate> audioCandidates = audioCandidatesFuture.get();
                                CombinedResult combinedResult = new CombinedResult(analysisResult, audioCandidates);

                                // Display result immediately when ready
                                synchronized(this) {
                                    WordAnalysisResult result = combinedResult.analysisResult();
                                    List<PronunciationCandidate> audioCandsFromResult = combinedResult.audioCandidates();

                                    int completed = completedCount.incrementAndGet();
                                    double percentage = (completed * 100.0) / maxToProcess;

                                    int successIndex = successCount.incrementAndGet();

                                    System.out.println("=".repeat(80));
                                    System.out.printf("Word %d/%d (%.1f%%) - '%s' (completed in %.2fs)%n",
                                        completed, maxToProcess, percentage, result.entry.hanzi(), result.duration / 1000.0);
                                    System.out.println("=".repeat(80));

                                    if (rawOutput) {
                                        parent.getAnalysisPrinter().printRaw(result.analysis);
                                    } else {
                                        parent.getAnalysisPrinter().printFormatted(result.analysis);
                                    }

                                    System.out.println();

                                    // Collect for export and store with audio candidates for later selection
                                    successfulAnalyses.add(result.analysis);
                                    wordsWithAudio.add(new WordWithAudioCandidates(result.analysis, audioCandsFromResult));
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("Error processing combined analysis and audio results", e);
                            }
                        });
                })
                .collect(Collectors.toList());
            
            // Wait for all displays to complete
            CompletableFuture.allOf(displayFutures.toArray(new CompletableFuture[0])).join();

            if (!skipAudio && !wordsWithAudio.isEmpty()) {
                InteractiveAudioUI audioUI = new InteractiveAudioUI();
                for (int i = 0; i < wordsWithAudio.size(); i++) {
                    WordWithAudioCandidates wordWithAudio = wordsWithAudio.get(i);
                    WordAnalysis updated = runAudioSelectionWithCandidates(audioUI, wordWithAudio.analysis(), wordWithAudio.candidates());
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
            parent.getAnalysisPrinter().printRaw(analysis);
        } else {
            parent.getAnalysisPrinter().printFormatted(analysis);
        }
        
        System.out.println();
    }

    private WordAnalysis runAudioSelection(AudioOrchestrator orchestrator, InteractiveAudioUI audioUI, WordAnalysis analysis) {
        List<PronunciationCandidate> rawCandidates = orchestrator.candidatesFor(analysis.word(), analysis.pinyin());
        if (rawCandidates.isEmpty()) {
            System.out.printf("No pronunciation candidates available for '%s'.%n%n", analysis.word().characters());
            return analysis;
        }

        List<PronunciationCandidate> candidates = parent.getPrePlayback().preprocessCandidates(analysis.word(), analysis.pinyin(), rawCandidates);
        if (candidates.isEmpty()) {
            System.out.printf("No playable pronunciation candidates available for '%s'.%n%n", analysis.word().characters());
            return analysis;
        }

        PronunciationCandidate choice;

        // Check for programmatic selection
        AudioSelection selection = audioSelections.get(analysis.word().characters());
        if (selection != null) {
            choice = findMatchingCandidate(candidates, selection);
            if (choice == null) {
                throw new IllegalStateException(
                    String.format("No matching audio candidate found for '%s' with provider '%s' and description '%s'",
                        analysis.word().characters(), selection.provider(), selection.description())
                );
            }
            System.out.printf("Programmatically selected audio for '%s' (%s): %s - %s%n",
                analysis.word().characters(), analysis.pinyin().pinyin(), choice.label(), choice.description());
        } else {
            // Interactive selection
            System.out.printf("Selecting audio for '%s' (%s)%n", analysis.word().characters(), analysis.pinyin().pinyin());
            SystemAudioPlayer player = new SystemAudioPlayer();
            SelectionSession session = new SelectionSession(candidates, player);
            try {
                choice = audioUI.run(session, analysis.word(), analysis.pinyin());
            } finally {
                player.stop();
            }

            if (choice == null) {
                System.out.println("No audio selected.");
                System.out.println();
                return analysis;
            }

            System.out.println("Selected audio: " + choice.file().toAbsolutePath());
        }

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

    private WordAnalysis runAudioSelectionWithCandidates(InteractiveAudioUI audioUI, WordAnalysis analysis, List<PronunciationCandidate> rawCandidates) {
        if (rawCandidates.isEmpty()) {
            System.out.printf("No pronunciation candidates available for '%s'.%n%n", analysis.word().characters());
            return analysis;
        }

        List<PronunciationCandidate> candidates = parent.getPrePlayback().preprocessCandidates(analysis.word(), analysis.pinyin(), rawCandidates);
        if (candidates.isEmpty()) {
            System.out.printf("No playable pronunciation candidates available for '%s'.%n%n", analysis.word().characters());
            return analysis;
        }

        PronunciationCandidate choice;

        // Check for programmatic selection
        AudioSelection selection = audioSelections.get(analysis.word().characters());
        if (selection != null) {
            choice = findMatchingCandidate(candidates, selection);
            if (choice == null) {
                throw new IllegalStateException(
                    String.format("No matching audio candidate found for '%s' with provider '%s' and description '%s'",
                        analysis.word().characters(), selection.provider(), selection.description())
                );
            }
            System.out.printf("Programmatically selected audio for '%s' (%s): %s - %s%n",
                analysis.word().characters(), analysis.pinyin().pinyin(), choice.label(), choice.description());
        } else {
            // Interactive selection
            System.out.printf("Selecting audio for '%s' (%s)%n", analysis.word().characters(), analysis.pinyin().pinyin());
            SystemAudioPlayer player = new SystemAudioPlayer();
            SelectionSession session = new SelectionSession(candidates, player);
            try {
                choice = audioUI.run(session, analysis.word(), analysis.pinyin());
            } finally {
                player.stop();
            }

            if (choice == null) {
                System.out.println("No audio selected.");
                System.out.println();
                return analysis;
            }

            System.out.println("Selected audio: " + choice.file().toAbsolutePath());
        }

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
        final long duration; // Duration in milliseconds

        WordAnalysisResult(PlecoEntry entry, WordAnalysis analysis, long duration) {
            this.entry = entry;
            this.analysis = analysis;
            this.duration = duration;
        }
    }

    /**
     * Record to hold both word analysis and pre-generated audio candidates
     */
    private record WordWithAudioCandidates(
        WordAnalysis analysis,
        List<PronunciationCandidate> candidates
    ) {}

    /**
     * Helper class to combine analysis result with audio candidates from parallel processing
     */
    private record CombinedResult(
        WordAnalysisResult analysisResult,
        List<PronunciationCandidate> audioCandidates
    ) {}

    /**
     * Record to hold audio selection preference for a word
     */
    private record AudioSelection(String provider, String description) {}
    
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

    private java.util.Map<String, AudioSelection> parseAudioSelections(String param) {
        if (param == null || param.trim().isEmpty()) {
            return java.util.Map.of();
        }

        java.util.Map<String, AudioSelection> selections = new java.util.HashMap<>();
        String[] entries = param.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid audio selection format: " + entry + ". Expected format: word:provider:description");
            }
            String word = parts[0].trim();
            String provider = parts[1].trim();
            String description = parts[2].trim();
            selections.put(word, new AudioSelection(provider, description));
        }
        return selections;
    }

    private PronunciationCandidate findMatchingCandidate(List<PronunciationCandidate> candidates, AudioSelection selection) {
        for (PronunciationCandidate candidate : candidates) {
            String candidateDesc = stripEmojis(candidate.description());
            if (candidate.label().equals(selection.provider()) && candidateDesc.equals(selection.description())) {
                return candidate;
            }
        }
        return null;
    }

    private String stripEmojis(String text) {
        if (text == null) {
            return "";
        }
        // Remove emoji characters (Unicode ranges for emoji)
        return text.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
    }
}
