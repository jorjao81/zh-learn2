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
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;
import com.zhlearn.infrastructure.common.ConfigurableGLMProvider;

import java.util.ArrayList;
import com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
import com.zhlearn.infrastructure.anki.AnkiPronunciationProvider;
import com.zhlearn.infrastructure.qwen.QwenAudioProvider;
import com.zhlearn.infrastructure.forvo.ForvoAudioProvider;

import java.util.List;
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
    private final List<AudioProvider> audioProviders;

    public MainCommand() {
        // Determine which AI provider to use based on available API keys
        String aiProvider = determineAIProvider();

        if ("glm-4.5".equals(aiProvider)) {
            // Use GLM-4.5 provider
            var glmProvider = new ConfigurableGLMProvider("glm-4-flash", "glm-4.5", "GLM-4.5 AI provider");
            this.exampleProvider = glmProvider;
            this.explanationProvider = glmProvider;
            this.decompositionProvider = glmProvider;
        } else {
            // Default to DeepSeek
            var providerConfig = new SimpleProviderConfig(
                "deepseek-chat",
                "DeepSeek AI provider",
                "https://api.deepseek.com/v1",
                "deepseek-chat",
                SimpleProviderConfig.readEnv("DEEPSEEK_API_KEY"),
                0.3,
                8000
            );

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
        }

        // Non-AI providers remain the same
        this.pinyinProvider = new Pinyin4jProvider();
        this.definitionProvider = new DummyDefinitionProvider(); // Use dummy for now

        // Multiple audio providers - restored functionality
        this.audioProviders = List.of(
            new AnkiPronunciationProvider(),
            new QwenAudioProvider(),
            new ForvoAudioProvider()
        );
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

    public List<AudioProvider> getAudioProviders() {
        return audioProviders;
    }

    // For backward compatibility - return the first audio provider
    public AudioProvider getAudioProvider() {
        return audioProviders.isEmpty() ? null : audioProviders.get(0);
    }

    private String determineAIProvider() {
        // Check for GLM API key first
        if (SimpleProviderConfig.readEnv("ZHIPU_API_KEY") != null) {
            return "glm-4.5";
        }
        // Check for Qwen API key
        if (SimpleProviderConfig.readEnv("DASHSCOPE_API_KEY") != null) {
            return "qwen";
        }
        // Default to DeepSeek
        return "deepseek";
    }

    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
