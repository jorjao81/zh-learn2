package com.zhlearn.cli;

import java.net.http.HttpClient;
import java.util.List;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.application.service.AnkiExporter;
import com.zhlearn.cli.audio.PrePlayback;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.anki.AnkiPronunciationProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioDownloadExecutor;
import com.zhlearn.infrastructure.audio.AudioNormalizer;
import com.zhlearn.infrastructure.audio.AudioPaths;
import com.zhlearn.infrastructure.common.AIProviderFactory;
import com.zhlearn.infrastructure.forvo.ForvoAudioProvider;
import com.zhlearn.infrastructure.minimax.MiniMaxAudioProvider;
import com.zhlearn.infrastructure.qwen.QwenAudioProvider;
import com.zhlearn.infrastructure.ratelimit.ProviderRateLimiter;
import com.zhlearn.infrastructure.ratelimit.RateLimiterConfig;
import com.zhlearn.infrastructure.ratelimit.RateLimiterRegistry;
import com.zhlearn.infrastructure.tencent.TencentAudioProvider;

/**
 * Application context for centralized dependency injection and bean management. Provides explicit,
 * compile-time dependency injection without runtime reflection.
 */
public class ApplicationContext {

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
    private final RateLimiterRegistry rateLimiterRegistry;
    private final List<AudioProvider> audioProviders;

    private ApplicationContext() {
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

        // Initialize rate limiter registry for per-provider rate limiting
        this.rateLimiterRegistry = new RateLimiterRegistry();
        ProviderRateLimiter qwenRateLimiter =
                rateLimiterRegistry.getOrCreate("qwen-tts", RateLimiterConfig.forQwen());
        ProviderRateLimiter tencentRateLimiter =
                rateLimiterRegistry.getOrCreate("tencent-tts", RateLimiterConfig.forTencent());
        ProviderRateLimiter minimaxRateLimiter =
                rateLimiterRegistry.getOrCreate("minimax-tts", RateLimiterConfig.forMiniMax());

        // Initialize audio executor and providers
        this.audioExecutor = new AudioDownloadExecutor();
        this.audioProviders =
                List.of(
                        new MiniMaxAudioProvider(
                                audioCache,
                                audioPaths,
                                audioExecutor.getExecutor(),
                                HttpClient.newHttpClient(),
                                null,
                                minimaxRateLimiter),
                        new AnkiPronunciationProvider(),
                        new ForvoAudioProvider(),
                        new QwenAudioProvider(
                                audioCache,
                                audioPaths,
                                audioExecutor.getExecutor(),
                                HttpClient.newHttpClient(),
                                null,
                                qwenRateLimiter),
                        new TencentAudioProvider(
                                audioCache,
                                audioPaths,
                                audioExecutor.getExecutor(),
                                null,
                                tencentRateLimiter));
    }

    /** Create a new ApplicationContext. */
    public static ApplicationContext create() {
        return new ApplicationContext();
    }

    public TerminalFormatter getTerminalFormatter() {
        return terminalFormatter;
    }

    public ExamplesHtmlFormatter getExamplesHtmlFormatter() {
        return examplesHtmlFormatter;
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

    public AudioPaths getAudioPaths() {
        return audioPaths;
    }

    public AudioCache getAudioCache() {
        return audioCache;
    }

    public PrePlayback getPrePlayback() {
        return prePlayback;
    }

    public AIProviderFactory getAiProviderFactory() {
        return aiProviderFactory;
    }

    public AudioDownloadExecutor getAudioExecutor() {
        return audioExecutor;
    }

    public List<AudioProvider> getAudioProviders() {
        return audioProviders;
    }
}
