package com.zhlearn.cli;

import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.infrastructure.common.ConfigurableExampleProvider;
import com.zhlearn.infrastructure.common.ConfigurableExplanationProvider;
import com.zhlearn.infrastructure.common.ConfigurableStructuralDecompositionProvider;
import com.zhlearn.infrastructure.common.SimpleProviderConfig;
import com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
import com.zhlearn.infrastructure.anki.AnkiPronunciationProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.ScopeType;

@Command(name = "zh-learn",
        mixinStandardHelpOptions = true,
        version = "1.0.0-SNAPSHOT",
        subcommands = { WordCommand.class, ProvidersCommand.class, ParseAnkiCommand.class, ParsePlecoCommand.class, AudioCommand.class, AudioSelectCommand.class, picocli.CommandLine.HelpCommand.class },
        scope = ScopeType.INHERIT)
public class MainCommand implements Runnable {

    // Single providers - no more registry!
    private final ExampleProvider exampleProvider;
    private final ExplanationProvider explanationProvider;
    private final StructuralDecompositionProvider decompositionProvider;
    private final PinyinProvider pinyinProvider;
    private final DefinitionProvider definitionProvider;
    private final AudioProvider audioProvider;

    public MainCommand() {
        // Configure the provider to use - DeepSeek by default
        var providerConfig = new SimpleProviderConfig(
            "deepseek-chat",
            "DeepSeek AI provider",
            "https://api.deepseek.com/v1",
            "deepseek-chat",
            SimpleProviderConfig.readEnv("DEEPSEEK_API_KEY"),
            0.3,
            8000
        );

        // Create providers directly using configurable wrapper providers
        this.exampleProvider = new ConfigurableExampleProvider(
            providerConfig.toInternalConfig(Example.class),
            "deepseek-chat",
            "DeepSeek AI-powered example provider");
        this.explanationProvider = new ConfigurableExplanationProvider(
            providerConfig.toInternalConfig(Explanation.class),
            "deepseek-chat",
            "DeepSeek AI-powered explanation provider");
        this.decompositionProvider = new ConfigurableStructuralDecompositionProvider(
            providerConfig.toInternalConfig(StructuralDecomposition.class),
            "deepseek-chat",
            "DeepSeek AI-powered structural decomposition provider");

        // Non-AI providers remain the same
        this.pinyinProvider = new Pinyin4jProvider();
        this.definitionProvider = new DummyDefinitionProvider(); // Use dummy for now
        this.audioProvider = new AnkiPronunciationProvider();
    }

    // Accessor methods for services
    public ExampleProvider getExampleProvider() {
        return exampleProvider;
    }

    public ExplanationProvider getExplanationProvider() {
        return explanationProvider;
    }

    public StructuralDecompositionProvider getDecompositionProvider() {
        return decompositionProvider;
    }

    public PinyinProvider getPinyinProvider() {
        return pinyinProvider;
    }

    public DefinitionProvider getDefinitionProvider() {
        return definitionProvider;
    }

    public AudioProvider getAudioProvider() {
        return audioProvider;
    }

    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
