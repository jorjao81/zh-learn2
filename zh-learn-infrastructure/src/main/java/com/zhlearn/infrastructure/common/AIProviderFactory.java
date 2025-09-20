package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.DummyExampleProvider;
import com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
import com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;

public class AIProviderFactory {

    public static ExampleProvider createExampleProvider(String providerName, String openRouterModel) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyExampleProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Example> config = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    ExampleProviderConfig.templatePath(),
                    ExampleProviderConfig.examplesDirectory(),
                    ExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from DeepSeek (deepseek-chat)"
                );
                yield new ConfigurableExampleProvider(config, providerName, "DeepSeek AI-powered example provider");
            }
            case "open-router" -> createOpenRouterExampleProvider(openRouterModel);
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Example> config = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    ExampleProviderConfig.templatePath(),
                    ExampleProviderConfig.examplesDirectory(),
                    ExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from Zhipu (glm-4-flash)"
                );
                ZhipuChatModelProvider<Example> delegate = new ZhipuChatModelProvider<>(config);
                yield new ConfigurableExampleProvider(delegate::process, providerName, "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Example> config = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    ExampleProviderConfig.templatePath(),
                    ExampleProviderConfig.examplesDirectory(),
                    ExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from Zhipu (glm-4.5)"
                );
                ZhipuChatModelProvider<Example> delegate = new ZhipuChatModelProvider<>(config);
                yield new ConfigurableExampleProvider(delegate::process, providerName, "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Example> config = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    ExampleProviderConfig.templatePath(),
                    ExampleProviderConfig.examplesDirectory(),
                    ExampleProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get examples from DashScope (" + providerName + ")"
                );
                yield new ConfigurableExampleProvider(config, providerName, "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown example provider: " + providerName +
                ". Available: dummy, deepseek-chat, open-router, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }

    public static ExplanationProvider createExplanationProvider(String providerName, String openRouterModel) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyExplanationProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Explanation> config = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    ExplanationProviderConfig.templatePath(),
                    ExplanationProviderConfig.examplesDirectory(),
                    ExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from DeepSeek (deepseek-chat)"
                );
                yield new ConfigurableExplanationProvider(config, providerName, "DeepSeek AI-powered explanation provider");
            }
            case "open-router" -> createOpenRouterExplanationProvider(openRouterModel);
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Explanation> config = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    ExplanationProviderConfig.templatePath(),
                    ExplanationProviderConfig.examplesDirectory(),
                    ExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from Zhipu (glm-4-flash)"
                );
                ZhipuChatModelProvider<Explanation> delegate = new ZhipuChatModelProvider<>(config);
                yield new ConfigurableExplanationProvider(delegate::process, providerName, "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Explanation> config = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    ExplanationProviderConfig.templatePath(),
                    ExplanationProviderConfig.examplesDirectory(),
                    ExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from Zhipu (glm-4.5)"
                );
                ZhipuChatModelProvider<Explanation> delegate = new ZhipuChatModelProvider<>(config);
                yield new ConfigurableExplanationProvider(delegate::process, providerName, "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Explanation> config = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    ExplanationProviderConfig.templatePath(),
                    ExplanationProviderConfig.examplesDirectory(),
                    ExplanationProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get explanation from DashScope (" + providerName + ")"
                );
                yield new ConfigurableExplanationProvider(config, providerName, "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown explanation provider: " + providerName +
                ". Available: dummy, deepseek-chat, open-router, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
        };
    }

    public static StructuralDecompositionProvider createDecompositionProvider(String providerName, String openRouterModel) {
        if (providerName == null) providerName = "deepseek-chat";

        return switch (providerName) {
            case "dummy" -> new DummyStructuralDecompositionProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> config = createProviderConfig(
                    DeepSeekConfig.getApiKey(),
                    DeepSeekConfig.getBaseUrl(),
                    "deepseek-chat",
                    StructuralDecompositionProviderConfig.templatePath(),
                    StructuralDecompositionProviderConfig.examplesDirectory(),
                    StructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from DeepSeek (deepseek-chat)"
                );
                yield new ConfigurableStructuralDecompositionProvider(
                    config, providerName, "DeepSeek AI-powered structural decomposition provider");
            }
            case "open-router" -> createOpenRouterDecompositionProvider(openRouterModel);
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> config = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4-flash",
                    StructuralDecompositionProviderConfig.templatePath(),
                    StructuralDecompositionProviderConfig.examplesDirectory(),
                    StructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from Zhipu (glm-4-flash)"
                );
                ZhipuChatModelProvider<StructuralDecomposition> delegate = new ZhipuChatModelProvider<>(config);
                yield new ConfigurableStructuralDecompositionProvider(delegate::process, providerName, "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> config = createProviderConfig(
                    ZhipuConfig.getApiKey(),
                    ZhipuConfig.getBaseUrl(),
                    "glm-4.5",
                    StructuralDecompositionProviderConfig.templatePath(),
                    StructuralDecompositionProviderConfig.examplesDirectory(),
                    StructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from Zhipu (glm-4.5)"
                );
                ZhipuChatModelProvider<StructuralDecomposition> delegate = new ZhipuChatModelProvider<>(config);
                yield new ConfigurableStructuralDecompositionProvider(delegate::process, providerName, "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> config = createProviderConfig(
                    DashScopeConfig.getApiKey(),
                    DashScopeConfig.getBaseUrl(),
                    providerName,
                    StructuralDecompositionProviderConfig.templatePath(),
                    StructuralDecompositionProviderConfig.examplesDirectory(),
                    StructuralDecompositionProviderConfig.responseMapper(),
                    providerName,
                    "Failed to get structural decomposition from DashScope (" + providerName + ")"
                );
                yield new ConfigurableStructuralDecompositionProvider(config, providerName, "Qwen AI provider (" + providerName + ")");
            }
            default -> throw new RuntimeException("Unknown decomposition provider: " + providerName +
                ". Available: dummy, deepseek-chat, open-router, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo");
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

    private static ConfigurableExampleProvider createOpenRouterExampleProvider(String openRouterModel) {
        if (openRouterModel == null || openRouterModel.isBlank()) {
            throw new IllegalArgumentException("OpenRouter provider requires a model to be specified via --open-router-model");
        }
        requireAPIKey("OPENROUTER_API_KEY", "open-router");
        ProviderConfig<Example> config = createProviderConfig(
            OpenRouterConfig.getApiKey(),
            OpenRouterConfig.getBaseUrl(),
            openRouterModel,
            ExampleProviderConfig.templatePath(),
            ExampleProviderConfig.examplesDirectory(),
            ExampleProviderConfig.responseMapper(),
            "open-router",
            "Failed to get example from OpenRouter (" + openRouterModel + ")"
        );
        return new ConfigurableExampleProvider(config, "open-router", "OpenRouter AI-powered example provider");
    }

    private static ConfigurableExplanationProvider createOpenRouterExplanationProvider(String openRouterModel) {
        if (openRouterModel == null || openRouterModel.isBlank()) {
            throw new IllegalArgumentException("OpenRouter provider requires a model to be specified via --open-router-model");
        }
        requireAPIKey("OPENROUTER_API_KEY", "open-router");
        ProviderConfig<Explanation> config = createProviderConfig(
            OpenRouterConfig.getApiKey(),
            OpenRouterConfig.getBaseUrl(),
            openRouterModel,
            ExplanationProviderConfig.templatePath(),
            ExplanationProviderConfig.examplesDirectory(),
            ExplanationProviderConfig.responseMapper(),
            "open-router",
            "Failed to get explanation from OpenRouter (" + openRouterModel + ")"
        );
        return new ConfigurableExplanationProvider(config, "open-router", "OpenRouter AI-powered explanation provider");
    }

    private static ConfigurableStructuralDecompositionProvider createOpenRouterDecompositionProvider(String openRouterModel) {
        if (openRouterModel == null || openRouterModel.isBlank()) {
            throw new IllegalArgumentException("OpenRouter provider requires a model to be specified via --open-router-model");
        }
        requireAPIKey("OPENROUTER_API_KEY", "open-router");
        ProviderConfig<StructuralDecomposition> config = createProviderConfig(
            OpenRouterConfig.getApiKey(),
            OpenRouterConfig.getBaseUrl(),
            openRouterModel,
            StructuralDecompositionProviderConfig.templatePath(),
            StructuralDecompositionProviderConfig.examplesDirectory(),
            StructuralDecompositionProviderConfig.responseMapper(),
            "open-router",
            "Failed to get structural decomposition from OpenRouter (" + openRouterModel + ")"
        );
        return new ConfigurableStructuralDecompositionProvider(config, "open-router", "OpenRouter AI-powered structural decomposition provider");
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
}