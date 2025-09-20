package com.zhlearn.cli;

import com.zhlearn.domain.provider.DefinitionFormatterProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.common.AIProviderFactory;

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

    public MainCommand() {
        // Initialize audio providers (keep existing working approach)
        this.audioProviders = List.of(
            new AnkiPronunciationProvider(),
            new ForvoAudioProvider(),
            new QwenAudioProvider(),
            new TencentAudioProvider()
        );
    }

    // Audio provider methods - keep existing working approach
    public List<AudioProvider> getAudioProviders() {
        return audioProviders;
    }

    public AudioProvider getAudioProvider() {
        return audioProviders.isEmpty() ? null : audioProviders.get(0);
    }

    // AI Provider factory methods - create on demand and crash if fails
    public ExampleProvider createExampleProvider(String providerName) {
        return AIProviderFactory.createExampleProvider(providerName);
    }

    public ExplanationProvider createExplanationProvider(String providerName) {
        return AIProviderFactory.createExplanationProvider(providerName);
    }

    public StructuralDecompositionProvider createDecompositionProvider(String providerName) {
        return AIProviderFactory.createDecompositionProvider(providerName);
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
        return AIProviderFactory.createDefinitionFormatterProvider(providerName);
    }




    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
