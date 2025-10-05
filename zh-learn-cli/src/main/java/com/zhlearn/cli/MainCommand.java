package com.zhlearn.cli;

import com.zhlearn.domain.provider.DefinitionFormatterProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.common.AIProviderFactory;
import com.zhlearn.infrastructure.audio.AudioDownloadExecutor;
import com.zhlearn.application.audio.AnkiMediaLocator;

import java.util.List;
import com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
import com.zhlearn.infrastructure.anki.AnkiPronunciationProvider;
import com.zhlearn.infrastructure.qwen.QwenAudioProvider;
import com.zhlearn.infrastructure.forvo.ForvoAudioProvider;
import com.zhlearn.infrastructure.tencent.TencentAudioProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.ScopeType;

@Command(name = "zh-learn",
        mixinStandardHelpOptions = true,
        version = "1.0.0-SNAPSHOT",
        subcommands = { WordCommand.class, ProvidersCommand.class, ParseAnkiCommand.class, ParsePlecoCommand.class, AudioCommand.class, AudioSelectCommand.class, picocli.CommandLine.HelpCommand.class },
        scope = ScopeType.INHERIT)
public class MainCommand implements Runnable {

    // Audio providers - keep as list like before
    private final List<AudioProvider> audioProviders;
    private final AudioDownloadExecutor audioExecutor;
    private final com.zhlearn.cli.audio.PrePlayback prePlayback;
    private final com.zhlearn.infrastructure.audio.AudioPaths audioPaths;
    private final com.zhlearn.infrastructure.audio.AudioCache audioCache;
    private final AIProviderFactory aiProviderFactory;
    private final TerminalFormatter terminalFormatter;
    private final com.zhlearn.application.format.ExamplesHtmlFormatter examplesHtmlFormatter;
    private final AnalysisPrinter analysisPrinter;
    private final AnkiMediaLocator ankiMediaLocator;

    public MainCommand() {
        // Initialize audio utilities
        this.audioPaths = new com.zhlearn.infrastructure.audio.AudioPaths();
        com.zhlearn.infrastructure.audio.AudioNormalizer audioNormalizer = new com.zhlearn.infrastructure.audio.AudioNormalizer();
        this.audioCache = new com.zhlearn.infrastructure.audio.AudioCache(audioPaths, audioNormalizer);
        this.prePlayback = new com.zhlearn.cli.audio.PrePlayback(audioCache, audioPaths);

        // Initialize AI provider factory
        this.aiProviderFactory = new AIProviderFactory();

        // Initialize formatters
        this.terminalFormatter = new TerminalFormatter();
        this.examplesHtmlFormatter = new com.zhlearn.application.format.ExamplesHtmlFormatter();
        this.analysisPrinter = new AnalysisPrinter(examplesHtmlFormatter, terminalFormatter);
        this.ankiMediaLocator = new AnkiMediaLocator();

        // Initialize audio executor and providers
        this.audioExecutor = new AudioDownloadExecutor();
        this.audioProviders = List.of(
            new AnkiPronunciationProvider(),
            new ForvoAudioProvider(audioExecutor),
            new QwenAudioProvider(audioExecutor),
            new TencentAudioProvider(audioExecutor)
        );
    }

    // Audio provider methods - keep existing working approach
    public List<AudioProvider> getAudioProviders() {
        return audioProviders;
    }

    public AudioProvider getAudioProvider() {
        return audioProviders.isEmpty() ? null : audioProviders.get(0);
    }

    public AudioDownloadExecutor getAudioExecutor() {
        return audioExecutor;
    }

    public com.zhlearn.cli.audio.PrePlayback getPrePlayback() {
        return prePlayback;
    }

    public TerminalFormatter getTerminalFormatter() {
        return terminalFormatter;
    }

    public com.zhlearn.application.format.ExamplesHtmlFormatter getExamplesHtmlFormatter() {
        return examplesHtmlFormatter;
    }

    public AnalysisPrinter getAnalysisPrinter() {
        return analysisPrinter;
    }

    public AnkiMediaLocator getAnkiMediaLocator() {
        return ankiMediaLocator;
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

    public StructuralDecompositionProvider createDecompositionProvider(String providerName, String model) {
        return aiProviderFactory.createDecompositionProvider(providerName, model);
    }

    public PinyinProvider createPinyinProvider(String providerName) {
        if (providerName == null) providerName = "pinyin4j";

        return switch (providerName) {
            case "pinyin4j" -> new Pinyin4jProvider();
            default -> throw new RuntimeException("Unknown pinyin provider: " + providerName +
                ". Available: pinyin4j");
        };
    }

    public DefinitionProvider createDefinitionProvider(String providerName) {
        if (providerName == null) providerName = "dummy";

        return switch (providerName) {
            case "dummy" -> new DummyDefinitionProvider();
            default -> throw new RuntimeException("Unknown definition provider: " + providerName +
                ". Available: dummy");
        };
    }

    public DefinitionFormatterProvider createDefinitionFormatterProvider(String providerName) {
        return aiProviderFactory.createDefinitionFormatterProvider(providerName);
    }

    public DefinitionFormatterProvider createDefinitionFormatterProvider(String providerName, String model) {
        return aiProviderFactory.createDefinitionFormatterProvider(providerName, model);
    }

    public void shutdown() {
        if (audioExecutor != null) {
            audioExecutor.shutdown();
        }
    }

    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
