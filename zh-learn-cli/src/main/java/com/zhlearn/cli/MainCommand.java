package com.zhlearn.cli;

import java.util.List;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.application.image.ImageOrchestrator;
import com.zhlearn.application.service.AnkiExporter;
import com.zhlearn.cli.audio.PrePlayback;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;
import com.zhlearn.domain.provider.DefinitionGeneratorProvider;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioDownloadExecutor;
import com.zhlearn.infrastructure.audio.AudioPaths;
import com.zhlearn.infrastructure.common.AIProviderFactory;
import com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
import com.zhlearn.infrastructure.image.GoogleImageSearchProvider;
import com.zhlearn.infrastructure.image.ImageDownloader;
import com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.ScopeType;

@Command(
        name = "zh-learn",
        mixinStandardHelpOptions = true,
        version = "1.0.0-SNAPSHOT",
        subcommands = {
            WordCommand.class,
            ProvidersCommand.class,
            ParseAnkiCommand.class,
            ParsePlecoCommand.class,
            ImproveAnkiCommand.class,
            AudioCommand.class,
            AudioSelectCommand.class,
            picocli.CommandLine.HelpCommand.class
        },
        scope = ScopeType.INHERIT)
public class MainCommand implements Runnable {

    // Audio providers - keep as list like before
    private final List<AudioProvider> audioProviders;
    private final AudioDownloadExecutor audioExecutor;
    private final PrePlayback prePlayback;
    private final AudioPaths audioPaths;
    private final AudioCache audioCache;
    private final AIProviderFactory aiProviderFactory;
    private final TerminalFormatter terminalFormatter;
    private final ExamplesHtmlFormatter examplesHtmlFormatter;
    private final AnalysisPrinter analysisPrinter;
    private final AnkiMediaLocator ankiMediaLocator;
    private final AnkiExporter ankiExporter;

    /**
     * Default constructor for tests. Uses ApplicationContext to create a fully initialized
     * instance.
     */
    public MainCommand() {
        this(ApplicationContext.create());
    }

    /**
     * Constructor for production use with ApplicationContext. Accepts all dependencies from
     * centralized dependency injection.
     */
    public MainCommand(ApplicationContext context) {
        this(
                context.getTerminalFormatter(),
                context.getExamplesHtmlFormatter(),
                context.getAnalysisPrinter(),
                context.getAnkiMediaLocator(),
                context.getAnkiExporter(),
                context.getAudioPaths(),
                context.getAudioCache(),
                context.getPrePlayback(),
                context.getAiProviderFactory(),
                context.getAudioExecutor(),
                context.getAudioProviders());
    }

    /**
     * Constructor for explicit dependency injection. Accepts all required dependencies directly.
     */
    public MainCommand(
            TerminalFormatter terminalFormatter,
            ExamplesHtmlFormatter examplesHtmlFormatter,
            AnalysisPrinter analysisPrinter,
            AnkiMediaLocator ankiMediaLocator,
            AnkiExporter ankiExporter,
            AudioPaths audioPaths,
            AudioCache audioCache,
            PrePlayback prePlayback,
            AIProviderFactory aiProviderFactory,
            AudioDownloadExecutor audioExecutor,
            List<AudioProvider> audioProviders) {

        this.terminalFormatter = terminalFormatter;
        this.examplesHtmlFormatter = examplesHtmlFormatter;
        this.analysisPrinter = analysisPrinter;
        this.ankiMediaLocator = ankiMediaLocator;
        this.ankiExporter = ankiExporter;
        this.audioPaths = audioPaths;
        this.audioCache = audioCache;
        this.prePlayback = prePlayback;
        this.aiProviderFactory = aiProviderFactory;
        this.audioExecutor = audioExecutor;
        this.audioProviders = audioProviders;
    }

    // Audio provider methods - keep existing working approach
    public List<AudioProvider> getAudioProviders() {
        return audioProviders;
    }

    public AudioDownloadExecutor getAudioExecutor() {
        return audioExecutor;
    }

    public PrePlayback getPrePlayback() {
        return prePlayback;
    }

    public TerminalFormatter getTerminalFormatter() {
        return terminalFormatter;
    }

    public AnalysisPrinter getAnalysisPrinter() {
        return analysisPrinter;
    }

    public AnkiMediaLocator getAnkiMediaLocator() {
        return ankiMediaLocator;
    }

    public AnkiExporter getAnkiExporter() {
        return ankiExporter;
    }

    public ImageOrchestrator createImageOrchestrator() {
        GoogleImageSearchProvider imageProvider = new GoogleImageSearchProvider();
        ImageDownloader imageDownloader = new ImageDownloader();
        return new ImageOrchestrator(imageProvider, imageDownloader, audioExecutor.getExecutor());
    }

    // AI Provider factory methods - create on demand and crash if fails
    public ExampleProvider createExampleProvider(String providerName) {
        return aiProviderFactory.createExampleProvider(providerName);
    }

    public ExampleProvider createExampleProvider(String providerName, String model) {
        return aiProviderFactory.createExampleProvider(providerName, model);
    }

    public ExplanationProvider createExplanationProvider(String providerName) {
        return aiProviderFactory.createExplanationProvider(providerName);
    }

    public ExplanationProvider createExplanationProvider(String providerName, String model) {
        return aiProviderFactory.createExplanationProvider(providerName, model);
    }

    public StructuralDecompositionProvider createDecompositionProvider(String providerName) {
        return aiProviderFactory.createDecompositionProvider(providerName);
    }

    public StructuralDecompositionProvider createDecompositionProvider(
            String providerName, String model) {
        return aiProviderFactory.createDecompositionProvider(providerName, model);
    }

    public PinyinProvider createPinyinProvider(String providerName) {
        if (providerName == null) providerName = "pinyin4j";

        return switch (providerName) {
            case "pinyin4j" -> new Pinyin4jProvider();
            default ->
                    throw new RuntimeException(
                            "Unknown pinyin provider: " + providerName + ". Available: pinyin4j");
        };
    }

    public DefinitionProvider createDefinitionProvider(String providerName) {
        if (providerName == null) providerName = "dummy";

        return switch (providerName) {
            case "dummy" -> new DummyDefinitionProvider();
            default ->
                    throw new RuntimeException(
                            "Unknown definition provider: " + providerName + ". Available: dummy");
        };
    }

    public DefinitionFormatterProvider createDefinitionFormatterProvider(String providerName) {
        return aiProviderFactory.createDefinitionFormatterProvider(providerName);
    }

    public DefinitionFormatterProvider createDefinitionFormatterProvider(
            String providerName, String model) {
        return aiProviderFactory.createDefinitionFormatterProvider(providerName, model);
    }

    public DefinitionGeneratorProvider createDefinitionGeneratorProvider(String providerName) {
        return aiProviderFactory.createDefinitionGeneratorProvider(providerName);
    }

    public DefinitionGeneratorProvider createDefinitionGeneratorProvider(
            String providerName, String model) {
        return aiProviderFactory.createDefinitionGeneratorProvider(providerName, model);
    }

    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
