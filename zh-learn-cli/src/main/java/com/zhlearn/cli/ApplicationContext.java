package com.zhlearn.cli;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.application.service.AnkiExporter;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioNormalizer;
import com.zhlearn.infrastructure.audio.AudioPaths;
import com.zhlearn.infrastructure.anki.AnkiPronunciationProvider;
import com.zhlearn.infrastructure.forvo.ForvoAudioProvider;
import com.zhlearn.infrastructure.qwen.QwenAudioProvider;
import com.zhlearn.infrastructure.tencent.TencentAudioProvider;
import com.zhlearn.infrastructure.common.AIProviderFactory;
import com.zhlearn.infrastructure.audio.AudioDownloadExecutor;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.cli.audio.PrePlayback;

import java.util.List;

/**
 * Application context for centralized dependency injection and bean management.
 * Provides explicit, compile-time dependency injection without runtime reflection.
 */
public class ApplicationContext {

    private final Configuration config;
    private final TerminalFormatter terminalFormatter;
    private final ExamplesHtmlFormatter examplesHtmlFormatter;
    private final AnalysisPrinter analysisPrinter;
    private final AnkiMediaLocator ankiMediaLocator;
    private final AnkiExporter ankiExporter;
    private final AudioPaths audioPaths;
    private final AudioNormalizer audioNormalizer;
    private final AudioCache audioCache;
    private final PrePlayback prePlayback;
    private final AIProviderFactory aiProviderFactory;
    private final AudioDownloadExecutor audioExecutor;
    private final List<AudioProvider> audioProviders;

    private ApplicationContext(Configuration config) {
        this.config = config;

        // Initialize core singleton services
        this.terminalFormatter = new TerminalFormatter();
        this.examplesHtmlFormatter = new ExamplesHtmlFormatter();
        this.ankiMediaLocator = new AnkiMediaLocator();
        this.analysisPrinter = new AnalysisPrinter(examplesHtmlFormatter, terminalFormatter);
        this.ankiExporter = new AnkiExporter(examplesHtmlFormatter, ankiMediaLocator);

        // Initialize audio utilities
        this.audioPaths = new AudioPaths();
        this.audioNormalizer = new AudioNormalizer();
        this.audioCache = new AudioCache(audioPaths, audioNormalizer);
        this.prePlayback = new PrePlayback(audioCache, audioPaths);

        // Initialize AI provider factory
        this.aiProviderFactory = new AIProviderFactory();

        // Initialize audio executor and providers
        this.audioExecutor = new AudioDownloadExecutor();
        this.audioProviders = List.of(
            new AnkiPronunciationProvider(),
            new ForvoAudioProvider(audioExecutor),
            new QwenAudioProvider(audioExecutor),
            new TencentAudioProvider(audioExecutor)
        );
    }

    /**
     * Create a new ApplicationContext with default configuration.
     */
    public static ApplicationContext create() {
        return create(Configuration.defaultConfig());
    }

    /**
     * Create a new ApplicationContext with custom configuration.
     */
    public static ApplicationContext create(Configuration config) {
        return new ApplicationContext(config);
    }

    // Getters for all managed beans
    public Configuration getConfig() { return config; }
    public TerminalFormatter getTerminalFormatter() { return terminalFormatter; }
    public ExamplesHtmlFormatter getExamplesHtmlFormatter() { return examplesHtmlFormatter; }
    public AnalysisPrinter getAnalysisPrinter() { return analysisPrinter; }
    public AnkiMediaLocator getAnkiMediaLocator() { return ankiMediaLocator; }
    public AnkiExporter getAnkiExporter() { return ankiExporter; }
    public AudioPaths getAudioPaths() { return audioPaths; }
    public AudioNormalizer getAudioNormalizer() { return audioNormalizer; }
    public AudioCache getAudioCache() { return audioCache; }
    public PrePlayback getPrePlayback() { return prePlayback; }
    public AIProviderFactory getAiProviderFactory() { return aiProviderFactory; }
    public AudioDownloadExecutor getAudioExecutor() { return audioExecutor; }
    public List<AudioProvider> getAudioProviders() { return audioProviders; }

    /**
     * Create a MainCommand with all dependencies injected.
     */
    public MainCommand createMainCommand() {
        return new MainCommand(
            terminalFormatter,
            examplesHtmlFormatter,
            analysisPrinter,
            ankiMediaLocator,
            ankiExporter,
            audioPaths,
            audioCache,
            prePlayback,
            aiProviderFactory,
            audioExecutor,
            audioProviders
        );
    }
}