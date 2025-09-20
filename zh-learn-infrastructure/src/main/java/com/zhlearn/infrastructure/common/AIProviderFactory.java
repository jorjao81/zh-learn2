package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionFormatterProvider;
import com.zhlearn.infrastructure.dummy.DummyExampleProvider;
import com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
import com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;

public class AIProviderFactory {

    public static ExampleProvider createExampleProvider(String providerName) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyExampleProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Example> singleConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    SingleCharExampleProviderConfig.templatePath(),
                    SingleCharExampleProviderConfig.examplesDirectory(),
                    SingleCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from DeepSeek (deepseek-chat)"
                );
                ProviderConfig<Example> multiConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    MultiCharExampleProviderConfig.templatePath(),
                    MultiCharExampleProviderConfig.examplesDirectory(),
                    MultiCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from DeepSeek (deepseek-chat)"
                );
                yield new ConfigurableExampleProvider(singleConfig, multiConfig, providerName, "DeepSeek AI-powered example provider");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Example> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    SingleCharExampleProviderConfig.templatePath(),
                    SingleCharExampleProviderConfig.examplesDirectory(),
                    SingleCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from Zhipu (glm-4-flash)"
                );
                ProviderConfig<Example> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    MultiCharExampleProviderConfig.templatePath(),
                    MultiCharExampleProviderConfig.examplesDirectory(),
                    MultiCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from Zhipu (glm-4-flash)"
                );
                ZhipuChatModelProvider<Example> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Example> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExampleProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Example> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    SingleCharExampleProviderConfig.templatePath(),
                    SingleCharExampleProviderConfig.examplesDirectory(),
                    SingleCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from Zhipu (glm-4.5)"
                );
                ProviderConfig<Example> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    MultiCharExampleProviderConfig.templatePath(),
                    MultiCharExampleProviderConfig.examplesDirectory(),
                    MultiCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from Zhipu (glm-4.5)"
                );
                ZhipuChatModelProvider<Example> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Example> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExampleProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Example> singleConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    SingleCharExampleProviderConfig.templatePath(),
                    SingleCharExampleProviderConfig.examplesDirectory(),
                    SingleCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from DashScope (" + providerName + ")"
                );
                ProviderConfig<Example> multiConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    MultiCharExampleProviderConfig.templatePath(),
                    MultiCharExampleProviderConfig.examplesDirectory(),
                    MultiCharExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from DashScope (" + providerName + ")"
                );
                yield new ConfigurableExampleProvider(singleConfig, multiConfig, providerName, "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown example provider: " + providerName +
                ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }

    public static ExplanationProvider createExplanationProvider(String providerName) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyExplanationProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    SingleCharExplanationProviderConfig.templatePath(),
                    SingleCharExplanationProviderConfig.examplesDirectory(),
                    SingleCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from DeepSeek (deepseek-chat)"
                );
                ProviderConfig<Explanation> multiConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    MultiCharExplanationProviderConfig.templatePath(),
                    MultiCharExplanationProviderConfig.examplesDirectory(),
                    MultiCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from DeepSeek (deepseek-chat)"
                );
                yield new ConfigurableExplanationProvider(singleConfig, multiConfig, providerName,
                    "DeepSeek AI-powered explanation provider");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    SingleCharExplanationProviderConfig.templatePath(),
                    SingleCharExplanationProviderConfig.examplesDirectory(),
                    SingleCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from Zhipu (glm-4-flash)"
                );
                ProviderConfig<Explanation> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    MultiCharExplanationProviderConfig.templatePath(),
                    MultiCharExplanationProviderConfig.examplesDirectory(),
                    MultiCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from Zhipu (glm-4-flash)"
                );
                ZhipuChatModelProvider<Explanation> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Explanation> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExplanationProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    SingleCharExplanationProviderConfig.templatePath(),
                    SingleCharExplanationProviderConfig.examplesDirectory(),
                    SingleCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from Zhipu (glm-4.5)"
                );
                ProviderConfig<Explanation> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    MultiCharExplanationProviderConfig.templatePath(),
                    MultiCharExplanationProviderConfig.examplesDirectory(),
                    MultiCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from Zhipu (glm-4.5)"
                );
                ZhipuChatModelProvider<Explanation> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Explanation> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExplanationProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    SingleCharExplanationProviderConfig.templatePath(),
                    SingleCharExplanationProviderConfig.examplesDirectory(),
                    SingleCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from DashScope (" + providerName + ")"
                );
                ProviderConfig<Explanation> multiConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    MultiCharExplanationProviderConfig.templatePath(),
                    MultiCharExplanationProviderConfig.examplesDirectory(),
                    MultiCharExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from DashScope (" + providerName + ")"
                );
                yield new ConfigurableExplanationProvider(singleConfig, multiConfig, providerName,
                    "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown explanation provider: " + providerName +
                ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }

    public static StructuralDecompositionProvider createDecompositionProvider(String providerName) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyStructuralDecompositionProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    SingleCharStructuralDecompositionProviderConfig.templatePath(),
                    SingleCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    SingleCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from DeepSeek (deepseek-chat)"
                );
                ProviderConfig<StructuralDecomposition> multiConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    MultiCharStructuralDecompositionProviderConfig.templatePath(),
                    MultiCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    MultiCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from DeepSeek (deepseek-chat)"
                );
                yield new ConfigurableStructuralDecompositionProvider(singleConfig, multiConfig, providerName,
                    "DeepSeek AI-powered structural decomposition provider");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    SingleCharStructuralDecompositionProviderConfig.templatePath(),
                    SingleCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    SingleCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from Zhipu (glm-4-flash)"
                );
                ProviderConfig<StructuralDecomposition> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    MultiCharStructuralDecompositionProviderConfig.templatePath(),
                    MultiCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    MultiCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from Zhipu (glm-4-flash)"
                );
                ZhipuChatModelProvider<StructuralDecomposition> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<StructuralDecomposition> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableStructuralDecompositionProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    SingleCharStructuralDecompositionProviderConfig.templatePath(),
                    SingleCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    SingleCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from Zhipu (glm-4.5)"
                );
                ProviderConfig<StructuralDecomposition> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    MultiCharStructuralDecompositionProviderConfig.templatePath(),
                    MultiCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    MultiCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from Zhipu (glm-4.5)"
                );
                ZhipuChatModelProvider<StructuralDecomposition> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<StructuralDecomposition> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableStructuralDecompositionProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    SingleCharStructuralDecompositionProviderConfig.templatePath(),
                    SingleCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    SingleCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from DashScope (" + providerName + ")"
                );
                ProviderConfig<StructuralDecomposition> multiConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    MultiCharStructuralDecompositionProviderConfig.templatePath(),
                    MultiCharStructuralDecompositionProviderConfig.examplesDirectory(),
                    MultiCharStructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from DashScope (" + providerName + ")"
                );
                yield new ConfigurableStructuralDecompositionProvider(singleConfig, multiConfig, providerName,
                    "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown decomposition provider: " + providerName +
                ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }

    private static <T> ProviderConfig<T> createProviderConfig(
            String apiKey,
            String baseUrl,
            String modelName,
            String templateResourcePath,
            String examplesResourcePath,
            java.util.function.Function<String, T> responseMapper,
            String providerName,
            String errorMessagePrefix) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            0.3, // temperature
            8000, // maxTokens
            templateResourcePath,
            examplesResourcePath,
            responseMapper,
            providerName,
            errorMessagePrefix
        );
    }

    private static void requireAPIKey(String keyName, String providerName) {
        String key = readEnv(keyName);
        if (key == null || key.trim().isEmpty()) {
            throw new RuntimeException("Provider '" + providerName + "' requires " + keyName + " environment variable to be set");
        }
    }

    private static String readEnv(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    public static DefinitionFormatterProvider createDefinitionFormatterProvider(String providerName) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyDefinitionFormatterProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    SingleCharDefinitionFormatterProviderConfig.templatePath(),
                    SingleCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    SingleCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from DeepSeek (deepseek-chat)"
                );
                ProviderConfig<Definition> multiConfig = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    MultiCharDefinitionFormatterProviderConfig.templatePath(),
                    MultiCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    MultiCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from DeepSeek (deepseek-chat)"
                );
                yield new ConfigurableDefinitionFormatterProvider(singleConfig, multiConfig, providerName, "DeepSeek AI-powered definition formatter");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    SingleCharDefinitionFormatterProviderConfig.templatePath(),
                    SingleCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    SingleCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from Zhipu (glm-4-flash)"
                );
                ProviderConfig<Definition> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    MultiCharDefinitionFormatterProviderConfig.templatePath(),
                    MultiCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    MultiCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from Zhipu (glm-4-flash)"
                );
                ZhipuChatModelProvider<Definition> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Definition> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableDefinitionFormatterProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4 Flash AI definition formatter");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    SingleCharDefinitionFormatterProviderConfig.templatePath(),
                    SingleCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    SingleCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from Zhipu (glm-4.5)"
                );
                ProviderConfig<Definition> multiConfig = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    MultiCharDefinitionFormatterProviderConfig.templatePath(),
                    MultiCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    MultiCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from Zhipu (glm-4.5)"
                );
                ZhipuChatModelProvider<Definition> singleDelegate = new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Definition> multiDelegate = new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableDefinitionFormatterProvider(singleDelegate::process, multiDelegate::process,
                    providerName, "GLM-4.5 AI definition formatter");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    SingleCharDefinitionFormatterProviderConfig.templatePath(),
                    SingleCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    SingleCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from DashScope (" + providerName + ")"
                );
                ProviderConfig<Definition> multiConfig = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    MultiCharDefinitionFormatterProviderConfig.templatePath(),
                    MultiCharDefinitionFormatterProviderConfig.examplesDirectory(),
                    MultiCharDefinitionFormatterProviderConfig.responseMapper(),
                    providerName,
                    "Failed to format definition from DashScope (" + providerName + ")"
                );
                yield new ConfigurableDefinitionFormatterProvider(singleConfig, multiConfig, providerName, "Qwen AI definition formatter (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown definition formatter provider: " + providerName +
                ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }
}
