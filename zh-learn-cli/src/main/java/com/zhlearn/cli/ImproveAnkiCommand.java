package com.zhlearn.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.application.audio.AudioOrchestrator;
import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;
import com.zhlearn.application.export.AnkiExportEntry;
import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.application.service.ParallelWordAnalysisService;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.cli.audio.InteractiveAudioUI;
import com.zhlearn.cli.audio.SystemAudioPlayer;
import com.zhlearn.cli.util.AudioSelectionUtils;
import com.zhlearn.cli.util.AudioSelectionUtils.AudioSelection;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;
import com.zhlearn.domain.provider.DefinitionGeneratorProvider;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.domain.service.WordAnalysisService;
import com.zhlearn.infrastructure.anki.AnkiNote;
import com.zhlearn.infrastructure.anki.AnkiNoteDictionary;
import com.zhlearn.infrastructure.anki.AnkiNoteParser;
import com.zhlearn.infrastructure.dictionary.DictionaryDefinitionProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryExampleProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryExplanationProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryPinyinProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryStructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.DummyAudioProvider;
import com.zhlearn.infrastructure.passthrough.PassthroughDefinitionFormatterProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * CLI command to improve specific fields in existing Anki export files. Selectively regenerates
 * fields (audio, explanation, examples, decomposition) while preserving unchanged fields from the
 * original export.
 */
@Command(
        name = "improve-anki",
        description = "Improve specific fields in existing Anki export files (Chinese 2 note type)")
public class ImproveAnkiCommand implements Runnable {

    @Parameters(index = "0", description = "Path to the Anki export file (TSV format, Chinese 2)")
    private String filePath;

    @Option(
            names = {"--improve-audio"},
            description = "Regenerate audio pronunciations for all words")
    private boolean improveAudio = false;

    @Option(
            names = {"--improve-explanation"},
            description = "Regenerate etymological explanations for all words")
    private boolean improveExplanation = false;

    @Option(
            names = {"--improve-examples"},
            description = "Regenerate example sentences for all words")
    private boolean improveExamples = false;

    @Option(
            names = {"--improve-decomposition"},
            description = "Regenerate structural decomposition for all words")
    private boolean improveDecomposition = false;

    @Option(
            names = {"--improve-definition"},
            description =
                    "Regenerate definitions for all words (generate if missing, format if present)")
    private boolean improveDefinition = false;

    @Option(
            names = {"--definition-formatter-provider"},
            description =
                    "Set specific provider for definition formatting (default: deepseek-chat). Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "deepseek-chat")
    private String definitionFormatterProvider;

    @Option(
            names = {"--definition-generator-provider"},
            description =
                    "Set specific provider for definition generation when missing (default: same as formatter). Available: deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview")
    private String definitionGeneratorProvider;

    @Option(
            names = {"--decomposition-provider"},
            description =
                    "Set specific provider for structural decomposition (default: deepseek-chat). Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "deepseek-chat")
    private String decompositionProvider;

    @Option(
            names = {"--example-provider"},
            description =
                    "Set specific provider for examples (default: deepseek-chat). Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "deepseek-chat")
    private String exampleProvider;

    @Option(
            names = {"--explanation-provider"},
            description =
                    "Set specific provider for explanation (default: deepseek-chat). Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "deepseek-chat")
    private String explanationProvider;

    @Option(
            names = {"--audio-provider"},
            description =
                    "Set specific provider for audio pronunciation (default: anki). Available: anki, forvo, qwen-tts",
            defaultValue = "anki")
    private String audioProvider;

    @Option(
            names = {"--model"},
            description =
                    "AI model to use with provider (e.g., for OpenRouter: gpt-4, claude-3-sonnet, llama-2-70b-chat)")
    private String model;

    @Option(
            names = {"--parallel-threads"},
            description = "Number of parallel threads for processing (default: 10)",
            defaultValue = "10")
    private int parallelThreads;

    @Option(
            names = {"--disable-parallelism"},
            description = "Disable parallel processing, use sequential processing instead")
    private boolean disableParallelism = false;

    @Option(
            names = {"--export-anki"},
            description = "Export improved results to Anki-compatible TSV file (Chinese 2 format)")
    private String ankiExportFile;

    @Option(
            names = {"--skip-audio"},
            description = "Skip interactive audio selection")
    private boolean skipAudio = false;

    @Option(
            names = {"--audio-selections"},
            description =
                    "Pre-configured audio selections (format: word:provider:description;word:provider:description)")
    private String audioSelectionsParam;

    @Option(
            names = {"--raw", "--raw-output"},
            description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;

    private Map<String, AudioSelection> audioSelections;

    @picocli.CommandLine.ParentCommand private MainCommand parent;

    @Override
    public void run() {
        try {
            // Validate that at least one improvement flag is set
            if (!improveAudio
                    && !improveExplanation
                    && !improveExamples
                    && !improveDecomposition
                    && !improveDefinition) {
                throw new IllegalArgumentException(
                        "At least one --improve-* flag must be specified. Available: --improve-audio, --improve-explanation, --improve-examples, --improve-decomposition, --improve-definition");
            }

            Path path = Paths.get(filePath);
            AnkiNoteParser parser = new AnkiNoteParser();
            List<AnkiNote> notes = parser.parseFile(path);

            System.out.println("Successfully parsed " + notes.size() + " notes from: " + filePath);
            System.out.println(
                    "Improving fields: "
                            + (improveAudio ? "audio " : "")
                            + (improveExplanation ? "explanation " : "")
                            + (improveExamples ? "examples " : "")
                            + (improveDecomposition ? "decomposition " : "")
                            + (improveDefinition ? "definition " : ""));
            System.out.println();

            // Parse audio selections if provided
            audioSelections = AudioSelectionUtils.parseAudioSelections(audioSelectionsParam);

            // Create dictionary from existing notes for unchanged fields
            AnkiNoteDictionary dictionary = new AnkiNoteDictionary(notes);

            // Set up providers - use dictionary for unchanged fields, real providers for improved
            // fields
            ExampleProvider exampleProv =
                    improveExamples
                            ? (model != null
                                    ? parent.createExampleProvider(exampleProvider, model)
                                    : parent.createExampleProvider(exampleProvider))
                            : new DictionaryExampleProvider(dictionary);

            ExplanationProvider explanationProv =
                    improveExplanation
                            ? (model != null
                                    ? parent.createExplanationProvider(explanationProvider, model)
                                    : parent.createExplanationProvider(explanationProvider))
                            : new DictionaryExplanationProvider(dictionary);

            StructuralDecompositionProvider decompositionProv =
                    improveDecomposition
                            ? (model != null
                                    ? parent.createDecompositionProvider(
                                            decompositionProvider, model)
                                    : parent.createDecompositionProvider(decompositionProvider))
                            : new DictionaryStructuralDecompositionProvider(dictionary);

            // Always use dictionary for pinyin (never improved)
            PinyinProvider pinyinProv = new DictionaryPinyinProvider(dictionary);

            // Definition providers: use AI if improving, dictionary+passthrough if not
            DefinitionProvider definitionProv = new DictionaryDefinitionProvider(dictionary);
            DefinitionFormatterProvider defFormatterProv;
            DefinitionGeneratorProvider defGeneratorProv;

            if (improveDefinition) {
                // Use AI providers for definition generation and formatting
                defFormatterProv =
                        model != null
                                ? parent.createDefinitionFormatterProvider(
                                        definitionFormatterProvider, model)
                                : parent.createDefinitionFormatterProvider(
                                        definitionFormatterProvider);

                String defGenProvider =
                        definitionGeneratorProvider != null
                                ? definitionGeneratorProvider
                                : definitionFormatterProvider;
                defGeneratorProv =
                        model != null
                                ? parent.createDefinitionGeneratorProvider(defGenProvider, model)
                                : parent.createDefinitionGeneratorProvider(defGenProvider);
            } else {
                // Use passthrough (no modification)
                defFormatterProv = new PassthroughDefinitionFormatterProvider();
                defGeneratorProv = null;
            }

            // Audio provider: use dummy if not improving, real provider if improving
            AudioProvider audioProv =
                    improveAudio ? resolveAudioProvider(audioProvider) : new DummyAudioProvider();

            // Create word analysis service
            WordAnalysisServiceImpl baseService =
                    new WordAnalysisServiceImpl(
                            exampleProv,
                            explanationProv,
                            decompositionProv,
                            pinyinProv,
                            definitionProv,
                            defFormatterProv,
                            defGeneratorProv,
                            audioProv);

            WordAnalysisService wordAnalysisService;
            ParallelWordAnalysisService parallelService = null;

            if (disableParallelism) {
                wordAnalysisService = baseService;
                System.out.println("Using sequential processing (parallelism disabled)");
            } else {
                parallelService = new ParallelWordAnalysisService(baseService, parallelThreads);
                wordAnalysisService = parallelService;
                System.out.println(
                        "Using parallel processing with " + parallelThreads + " threads");
            }

            ProviderConfiguration config =
                    new ProviderConfiguration(
                            exampleProvider,
                            "anki-export", // pinyin from dictionary
                            "anki-export", // definition from dictionary
                            "passthrough", // no definition formatting
                            decompositionProvider,
                            exampleProvider,
                            explanationProvider,
                            audioProvider);

            // Check if interactive audio needed (skip if all notes have pre-configured selections)
            boolean allNotesHaveAudioSelections =
                    notes.stream().allMatch(note -> audioSelections.containsKey(note.simplified()));
            if (improveAudio && !skipAudio && !allNotesHaveAudioSelections && !notes.isEmpty()) {
                ensureInteractiveAudioSupported();
            }

            // Thread-safe collection to store successful analyses for export
            List<WordAnalysis> successfulAnalyses = new CopyOnWriteArrayList<>();

            if (disableParallelism) {
                // Sequential processing
                processNotesSequentially(
                        notes, wordAnalysisService, config, notes.size(), successfulAnalyses);
            } else {
                // Parallel processing
                processNotesInParallel(
                        notes,
                        wordAnalysisService,
                        config,
                        notes.size(),
                        parallelThreads,
                        successfulAnalyses);

                // Shutdown the parallel service
                if (parallelService != null) {
                    parallelService.shutdown();
                }
            }

            // Export to Anki file with custom merge logic for unchanged fields
            if (ankiExportFile != null && !ankiExportFile.trim().isEmpty()) {
                Map<String, AnkiNote> originalNotesMap =
                        notes.stream()
                                .collect(
                                        Collectors.toMap(
                                                AnkiNote::simplified,
                                                Function.identity(),
                                                (a, b) -> a));
                exportImprovedNotes(successfulAnalyses, originalNotesMap, ankiExportFile.trim());
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to process Anki export at " + filePath, e);
        }
    }

    private void processNotesSequentially(
            List<AnkiNote> notes,
            WordAnalysisService wordAnalysisService,
            ProviderConfiguration config,
            int maxToProcess,
            List<WordAnalysis> successfulAnalyses) {
        int processedCount = 0;
        AudioOrchestrator audioOrchestrator =
                improveAudio && !skipAudio
                        ? new AudioOrchestrator(
                                parent.getAudioProviders(), parent.getAudioExecutor())
                        : null;
        InteractiveAudioUI audioUI = improveAudio && !skipAudio ? new InteractiveAudioUI() : null;

        for (AnkiNote note : notes) {
            Hanzi word = new Hanzi(note.simplified());
            WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
            printWordAnalysis(analysis, processedCount + 1, maxToProcess);

            WordAnalysis updated =
                    (improveAudio && !skipAudio)
                            ? runAudioSelection(audioOrchestrator, audioUI, analysis)
                            : analysis;

            successfulAnalyses.add(updated);
            processedCount++;
        }

        System.out.println("Processed " + processedCount + " words successfully.");
    }

    private void processNotesInParallel(
            List<AnkiNote> notes,
            WordAnalysisService wordAnalysisService,
            ProviderConfiguration config,
            int maxToProcess,
            int threadCount,
            List<WordAnalysis> successfulAnalyses) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AudioOrchestrator audioOrchestrator =
                improveAudio && !skipAudio
                        ? new AudioOrchestrator(
                                parent.getAudioProviders(), parent.getAudioExecutor())
                        : null;

        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        List<WordWithAudioCandidates> wordsWithAudio = new CopyOnWriteArrayList<>();

        try {
            long overallStartTime = System.currentTimeMillis();

            List<CompletableFuture<Void>> displayFutures =
                    notes.stream()
                            .map(
                                    note -> {
                                        CompletableFuture<WordAnalysisResult> analysisFuture =
                                                CompletableFuture.supplyAsync(
                                                        () -> {
                                                            long wordStartTime =
                                                                    System.currentTimeMillis();
                                                            Hanzi word =
                                                                    new Hanzi(note.simplified());
                                                            WordAnalysis analysis =
                                                                    wordAnalysisService
                                                                            .getCompleteAnalysis(
                                                                                    word, config);
                                                            long wordDuration =
                                                                    System.currentTimeMillis()
                                                                            - wordStartTime;
                                                            return new WordAnalysisResult(
                                                                    note, analysis, wordDuration);
                                                        },
                                                        executor);

                                        CompletableFuture<List<PronunciationCandidate>>
                                                audioCandidatesFuture =
                                                        (improveAudio && !skipAudio)
                                                                ? CompletableFuture.supplyAsync(
                                                                        () -> {
                                                                            Hanzi word =
                                                                                    new Hanzi(
                                                                                            note
                                                                                                    .simplified());
                                                                            if (wordAnalysisService
                                                                                    instanceof
                                                                                    ParallelWordAnalysisService
                                                                                            parallelService) {
                                                                                Pinyin pinyin =
                                                                                        parallelService
                                                                                                .getPinyin(
                                                                                                        word,
                                                                                                        config
                                                                                                                .getPinyinProvider());
                                                                                System.out.printf(
                                                                                        "[INFO] Starting audio download for '%s' (%s)%n",
                                                                                        word
                                                                                                .characters(),
                                                                                        pinyin
                                                                                                .pinyin());
                                                                                List<
                                                                                                PronunciationCandidate>
                                                                                        candidates =
                                                                                                audioOrchestrator
                                                                                                        .candidatesFor(
                                                                                                                word,
                                                                                                                pinyin);
                                                                                System.out.printf(
                                                                                        "[INFO] Completed audio download for '%s' - found %d candidates%n",
                                                                                        word
                                                                                                .characters(),
                                                                                        candidates
                                                                                                .size());
                                                                                return candidates;
                                                                            } else {
                                                                                Pinyin pinyin =
                                                                                        wordAnalysisService
                                                                                                .getPinyin(
                                                                                                        word,
                                                                                                        config
                                                                                                                .getPinyinProvider());
                                                                                System.out.printf(
                                                                                        "[INFO] Starting audio download for '%s' (%s)%n",
                                                                                        word
                                                                                                .characters(),
                                                                                        pinyin
                                                                                                .pinyin());
                                                                                List<
                                                                                                PronunciationCandidate>
                                                                                        candidates =
                                                                                                audioOrchestrator
                                                                                                        .candidatesFor(
                                                                                                                word,
                                                                                                                pinyin);
                                                                                System.out.printf(
                                                                                        "[INFO] Completed audio download for '%s' - found %d candidates%n",
                                                                                        word
                                                                                                .characters(),
                                                                                        candidates
                                                                                                .size());
                                                                                return candidates;
                                                                            }
                                                                        },
                                                                        executor)
                                                                : CompletableFuture.completedFuture(
                                                                        List.of());

                                        return CompletableFuture.allOf(
                                                        analysisFuture, audioCandidatesFuture)
                                                .thenAccept(
                                                        ignored -> {
                                                            WordAnalysisResult analysisResult =
                                                                    analysisFuture.join();
                                                            List<PronunciationCandidate>
                                                                    audioCandidates =
                                                                            audioCandidatesFuture
                                                                                    .join();
                                                            CombinedResult combinedResult =
                                                                    new CombinedResult(
                                                                            analysisResult,
                                                                            audioCandidates);

                                                            synchronized (this) {
                                                                WordAnalysisResult result =
                                                                        combinedResult
                                                                                .analysisResult();
                                                                List<PronunciationCandidate>
                                                                        audioCandsFromResult =
                                                                                combinedResult
                                                                                        .audioCandidates();

                                                                int completed =
                                                                        completedCount
                                                                                .incrementAndGet();
                                                                double percentage =
                                                                        (completed * 100.0)
                                                                                / maxToProcess;

                                                                successCount.incrementAndGet();

                                                                System.out.println("=".repeat(80));
                                                                System.out.printf(
                                                                        "Word %d/%d (%.1f%%) - '%s' (completed in %.2fs)%n",
                                                                        completed,
                                                                        maxToProcess,
                                                                        percentage,
                                                                        result.note.simplified(),
                                                                        result.duration / 1000.0);
                                                                System.out.println("=".repeat(80));

                                                                if (rawOutput) {
                                                                    parent.getAnalysisPrinter()
                                                                            .printRaw(
                                                                                    result.analysis);
                                                                } else {
                                                                    parent.getAnalysisPrinter()
                                                                            .printFormatted(
                                                                                    result.analysis);
                                                                }

                                                                System.out.println();

                                                                successfulAnalyses.add(
                                                                        result.analysis);
                                                                wordsWithAudio.add(
                                                                        new WordWithAudioCandidates(
                                                                                result.analysis,
                                                                                audioCandsFromResult));
                                                            }
                                                        });
                                    })
                            .collect(Collectors.toList());

            CompletableFuture.allOf(displayFutures.toArray(new CompletableFuture[0])).join();

            if (improveAudio && !skipAudio && !wordsWithAudio.isEmpty()) {
                InteractiveAudioUI audioUI = new InteractiveAudioUI();
                for (int i = 0; i < wordsWithAudio.size(); i++) {
                    WordWithAudioCandidates wordWithAudio = wordsWithAudio.get(i);
                    WordAnalysis updated =
                            runAudioSelectionWithCandidates(
                                    audioUI, wordWithAudio.analysis(), wordWithAudio.candidates());
                    successfulAnalyses.set(i, updated);
                }
            }

            long overallDuration = System.currentTimeMillis() - overallStartTime;
            System.out.println("=".repeat(80));
            System.out.printf(
                    "Processing complete! %d words successful in %.2fs%n",
                    successCount.get(), overallDuration / 1000.0);

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

    private WordAnalysis runAudioSelection(
            AudioOrchestrator orchestrator, InteractiveAudioUI audioUI, WordAnalysis analysis) {
        List<PronunciationCandidate> rawCandidates =
                orchestrator.candidatesFor(analysis.word(), analysis.pinyin());
        if (rawCandidates.isEmpty()) {
            System.out.printf(
                    "No pronunciation candidates available for '%s'.%n%n",
                    analysis.word().characters());
            return analysis;
        }

        List<PronunciationCandidate> candidates =
                parent.getPrePlayback()
                        .preprocessCandidates(analysis.word(), analysis.pinyin(), rawCandidates);
        if (candidates.isEmpty()) {
            System.out.printf(
                    "No playable pronunciation candidates available for '%s'.%n%n",
                    analysis.word().characters());
            return analysis;
        }

        PronunciationCandidate choice;

        AudioSelection selection = audioSelections.get(analysis.word().characters());
        if (selection != null) {
            choice = AudioSelectionUtils.findMatchingCandidate(candidates, selection);
            if (choice == null) {
                throw new IllegalStateException(
                        String.format(
                                "No matching audio candidate found for '%s' with provider '%s' and description '%s'",
                                analysis.word().characters(),
                                selection.provider(),
                                selection.description()));
            }
            System.out.printf(
                    "Programmatically selected audio for '%s' (%s): %s - %s%n",
                    analysis.word().characters(),
                    analysis.pinyin().pinyin(),
                    choice.label(),
                    choice.description());
        } else {
            System.out.printf(
                    "Selecting audio for '%s' (%s)%n",
                    analysis.word().characters(), analysis.pinyin().pinyin());
            SystemAudioPlayer player = new SystemAudioPlayer(parent.getAnkiMediaLocator());
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
                Optional.of(choice.file().toAbsolutePath()));
    }

    private WordAnalysis runAudioSelectionWithCandidates(
            InteractiveAudioUI audioUI,
            WordAnalysis analysis,
            List<PronunciationCandidate> rawCandidates) {
        if (rawCandidates.isEmpty()) {
            System.out.printf(
                    "No pronunciation candidates available for '%s'.%n%n",
                    analysis.word().characters());
            return analysis;
        }

        List<PronunciationCandidate> candidates =
                parent.getPrePlayback()
                        .preprocessCandidates(analysis.word(), analysis.pinyin(), rawCandidates);
        if (candidates.isEmpty()) {
            System.out.printf(
                    "No playable pronunciation candidates available for '%s'.%n%n",
                    analysis.word().characters());
            return analysis;
        }

        PronunciationCandidate choice;

        AudioSelection selection = audioSelections.get(analysis.word().characters());
        if (selection != null) {
            choice = AudioSelectionUtils.findMatchingCandidate(candidates, selection);
            if (choice == null) {
                throw new IllegalStateException(
                        String.format(
                                "No matching audio candidate found for '%s' with provider '%s' and description '%s'",
                                analysis.word().characters(),
                                selection.provider(),
                                selection.description()));
            }
            System.out.printf(
                    "Programmatically selected audio for '%s' (%s): %s - %s%n",
                    analysis.word().characters(),
                    analysis.pinyin().pinyin(),
                    choice.label(),
                    choice.description());
        } else {
            System.out.printf(
                    "Selecting audio for '%s' (%s)%n",
                    analysis.word().characters(), analysis.pinyin().pinyin());
            SystemAudioPlayer player = new SystemAudioPlayer(parent.getAnkiMediaLocator());
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
                Optional.of(choice.file().toAbsolutePath()));
    }

    private void ensureInteractiveAudioSupported() {
        if (System.console() != null) {
            return;
        }

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            if (terminal == null || "dumb".equalsIgnoreCase(terminal.getType())) {
                failNonInteractiveTerminal("audio");
            }
        } catch (IOException e) {
            System.err.println(
                    "Unable to initialize terminal for audio selection: " + e.getMessage());
            System.exit(1);
        } catch (IllegalStateException e) {
            failNonInteractiveTerminal("audio");
        }
    }

    private void failNonInteractiveTerminal(String featureType) {
        System.err.printf(
                "improve-anki requires an interactive terminal that supports raw mode for %s selection.%n",
                featureType);
        System.err.println(
                "Run zh-learn from a terminal (no piping or redirection) or disable this feature to continue.");
        System.exit(1);
    }

    /** Helper record to hold analysis results from parallel processing */
    private record WordAnalysisResult(AnkiNote note, WordAnalysis analysis, long duration) {}

    /** Record to hold both word analysis and pre-generated audio candidates */
    private record WordWithAudioCandidates(
            WordAnalysis analysis, List<PronunciationCandidate> candidates) {}

    /** Helper record to combine analysis result with audio candidates from parallel processing */
    private record CombinedResult(
            WordAnalysisResult analysisResult, List<PronunciationCandidate> audioCandidates) {}

    /**
     * Export the improved WordAnalysis results to an Anki-compatible TSV file. Merges improved
     * fields with original unchanged fields.
     */
    private void exportImprovedNotes(
            List<WordAnalysis> analyses, Map<String, AnkiNote> originalNotes, String filename) {
        try {
            Path outputFile = Path.of(filename);
            ExamplesHtmlFormatter examplesFormatter = new ExamplesHtmlFormatter();
            AnkiMediaLocator ankiMediaLocator = parent.getAnkiMediaLocator();
            Optional<Path> ankiMediaDir = ankiMediaLocator.locate();

            try (PrintWriter writer =
                    new PrintWriter(Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {
                // Write Anki TSV headers
                writer.println("#separator:tab");
                writer.println("#html:true");
                writer.println("#notetype column:1");

                // Write data rows
                for (WordAnalysis analysis : analyses) {
                    AnkiNote original = originalNotes.get(analysis.word().characters());
                    if (original == null) {
                        System.err.printf(
                                "Warning: No original note found for '%s', skipping export%n",
                                analysis.word().characters());
                        continue;
                    }

                    // Build pronunciation field
                    String soundNotation;
                    if (improveAudio && analysis.pronunciation().isPresent()) {
                        soundNotation =
                                buildSoundNotation(analysis.pronunciation().get(), ankiMediaDir);
                    } else {
                        soundNotation = original.pronunciation(); // Keep original
                    }

                    // Build examples field
                    String examplesHtml;
                    if (improveExamples) {
                        examplesHtml = examplesFormatter.format(analysis.examples());
                    } else {
                        examplesHtml = original.examples(); // Keep original HTML
                    }

                    // Build etymology (explanation) field
                    String etymologyText;
                    if (improveExplanation) {
                        etymologyText = analysis.explanation().explanation();
                    } else {
                        etymologyText = original.etymology(); // Keep original
                    }

                    // Build components (decomposition) field
                    String componentsText;
                    if (improveDecomposition) {
                        componentsText = analysis.structuralDecomposition().decomposition();
                    } else {
                        componentsText = original.components(); // Keep original
                    }

                    AnkiExportEntry entry =
                            new AnkiExportEntry(
                                    "Chinese 2",
                                    analysis.word().characters(),
                                    analysis.pinyin().pinyin(),
                                    soundNotation,
                                    analysis.definition().meaning(),
                                    examplesHtml,
                                    etymologyText,
                                    componentsText,
                                    original.similar(), // Always keep
                                    original.passive(), // Always keep
                                    original.alternatePronunciations(), // Always keep
                                    original.noHearing());
                    writer.println(formatAsTabSeparated(entry));
                }
            }

            System.out.println("=".repeat(80));
            System.out.printf(
                    "Exported %d words to %s (Anki Chinese 2 format)%n", analyses.size(), filename);

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to export Anki file to " + filename, e);
        }
    }

    private String buildSoundNotation(Path pronunciation, Optional<Path> ankiMediaDir)
            throws IOException {
        Path target = ensureAudioInAnkiMedia(pronunciation, ankiMediaDir);
        Path fileName = target.getFileName();
        if (fileName == null) {
            throw new IOException("Unable to derive filename for pronunciation audio: " + target);
        }
        return "[sound:" + fileName.toString() + "]";
    }

    private Path ensureAudioInAnkiMedia(Path audioFile, Optional<Path> ankiMediaDir)
            throws IOException {
        Path source = audioFile.toAbsolutePath().normalize();
        if (!Files.exists(source)) {
            throw new IOException("Pronunciation audio file not found: " + source);
        }

        if (ankiMediaDir.isEmpty()) {
            return source;
        }

        Path mediaDir = ankiMediaDir.get().toAbsolutePath().normalize();
        Files.createDirectories(mediaDir);

        if (source.startsWith(mediaDir)) {
            return source;
        }

        Path target = mediaDir.resolve(source.getFileName());
        if (Files.exists(target)) {
            if (Files.isSameFile(source, target)) {
                return target.toAbsolutePath();
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath();
        }

        Files.copy(source, target);
        return target.toAbsolutePath();
    }

    /**
     * Format an AnkiExportEntry as a tab-separated line for TSV output. Properly escapes special
     * characters that could break TSV format.
     */
    private String formatAsTabSeparated(AnkiExportEntry entry) {
        return String.join(
                "\t",
                escapeForTSV(entry.noteType()),
                escapeForTSV(entry.simplified()),
                escapeForTSV(entry.pinyin()),
                escapeForTSV(entry.pronunciation()),
                escapeForTSV(entry.formattedDefinition()),
                escapeForTSV(entry.examples()),
                escapeForTSV(entry.etymology()),
                escapeForTSV(entry.components()),
                escapeForTSV(entry.similar()),
                escapeForTSV(entry.passive()),
                escapeForTSV(entry.alternatePronunciations()),
                escapeForTSV(entry.noHearing()));
    }

    /**
     * Escape a string value for safe inclusion in TSV format. Handles tabs, newlines, and quotes
     * that could break the format.
     */
    private String escapeForTSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains("\t")
                || value.contains("\n")
                || value.contains("\r")
                || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private AudioProvider resolveAudioProvider(String providerName) {
        return parent.getAudioProviders().stream()
                .filter(provider -> provider.getName().equals(providerName))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unknown audio provider: " + providerName));
    }
}
