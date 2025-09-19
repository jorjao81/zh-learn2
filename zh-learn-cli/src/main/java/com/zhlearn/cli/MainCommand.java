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
import com.zhlearn.infrastructure.common.DashScopeConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;
import com.zhlearn.infrastructure.common.ConfigurableQwenProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
import com.zhlearn.infrastructure.dummy.DummyExampleProvider;
import com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
import com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;
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

    // Audio providers - keep as list like before
    private final List<AudioProvider> audioProviders;

    public MainCommand() {
        // Initialize audio providers (keep existing working approach)
        this.audioProviders = List.of(
            new AnkiPronunciationProvider(),
            new QwenAudioProvider(),
            new ForvoAudioProvider()
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
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyExampleProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                var config = new SimpleProviderConfig(
                    "deepseek-chat", "DeepSeek AI provider", "https://api.deepseek.com/v1",
                    "deepseek-chat", SimpleProviderConfig.readEnv("DEEPSEEK_API_KEY"), 0.3, 8000);
                yield new ConfigurableExampleProvider(
                    config.toInternalConfig(Example.class), "deepseek-chat", "DeepSeek AI-powered example provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                yield new ConfigurableGLMProvider("glm-4-flash", "glm-4.5", "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                yield new ConfigurableQwenProvider(providerName, providerName, "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown example provider: " + providerName +
                ". Available: dummy, deepseek-chat, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }

    public ExplanationProvider createExplanationProvider(String providerName) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyExplanationProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                var config = new SimpleProviderConfig(
                    "deepseek-chat", "DeepSeek AI provider", "https://api.deepseek.com/v1",
                    "deepseek-chat", SimpleProviderConfig.readEnv("DEEPSEEK_API_KEY"), 0.3, 8000);
                yield new ConfigurableExplanationProvider(
                    config.toInternalConfig(Explanation.class), "deepseek-chat", "DeepSeek AI-powered explanation provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                yield new ConfigurableGLMProvider("glm-4-flash", "glm-4.5", "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                yield new ConfigurableQwenProvider(providerName, providerName, "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown explanation provider: " + providerName +
                ". Available: dummy, deepseek-chat, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }

    public StructuralDecompositionProvider createDecompositionProvider(String providerName) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyStructuralDecompositionProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                var config = new SimpleProviderConfig(
                    "deepseek-chat", "DeepSeek AI provider", "https://api.deepseek.com/v1",
                    "deepseek-chat", SimpleProviderConfig.readEnv("DEEPSEEK_API_KEY"), 0.3, 8000);
                yield new ConfigurableStructuralDecompositionProvider(
                    config.toInternalConfig(StructuralDecomposition.class), "deepseek-chat", "DeepSeek AI-powered structural decomposition provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                yield new ConfigurableGLMProvider("glm-4-flash", "glm-4.5", "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                yield new ConfigurableQwenProvider(providerName, providerName, "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown decomposition provider: " + providerName +
                ". Available: dummy, deepseek-chat, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
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

    private void requireAPIKey(String keyName, String providerName) {
        String key = SimpleProviderConfig.readEnv(keyName);
        if (key == null || key.trim().isEmpty()) {
            throw new RuntimeException("Provider '" + providerName + "' requires " + keyName + " environment variable to be set");
        }
    }



    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
