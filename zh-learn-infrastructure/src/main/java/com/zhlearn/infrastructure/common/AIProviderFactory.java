package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;
import com.zhlearn.domain.provider.DefinitionGeneratorProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionFormatterProvider;
import com.zhlearn.infrastructure.dummy.DummyExampleProvider;
import com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
import com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;

public class AIProviderFactory {

    // Config instances - created once per factory instance
    private final DeepSeekConfig deepSeekConfig = new DeepSeekConfig();
    private final GeminiConfig geminiConfig = new GeminiConfig();
    private final ZhipuConfig zhipuConfig = new ZhipuConfig();
    private final DashScopeConfig dashScopeConfig = new DashScopeConfig();
    private final OpenRouterConfig openRouterConfig = new OpenRouterConfig();

    // ProviderConfig helper instances
    private final SingleCharExampleProviderConfig singleCharExampleConfig =
            new SingleCharExampleProviderConfig();
    private final MultiCharExampleProviderConfig multiCharExampleConfig =
            new MultiCharExampleProviderConfig();
    private final SingleCharExplanationProviderConfig singleCharExplanationConfig =
            new SingleCharExplanationProviderConfig();
    private final MultiCharExplanationProviderConfig multiCharExplanationConfig =
            new MultiCharExplanationProviderConfig();
    private final SingleCharStructuralDecompositionProviderConfig singleCharStructuralConfig =
            new SingleCharStructuralDecompositionProviderConfig();
    private final MultiCharStructuralDecompositionProviderConfig multiCharStructuralConfig =
            new MultiCharStructuralDecompositionProviderConfig();
    private final SingleCharDefinitionFormatterProviderConfig singleCharDefinitionConfig =
            new SingleCharDefinitionFormatterProviderConfig();
    private final MultiCharDefinitionFormatterProviderConfig multiCharDefinitionConfig =
            new MultiCharDefinitionFormatterProviderConfig();
    private final SingleCharDefinitionGeneratorProviderConfig singleCharDefinitionGeneratorConfig =
            new SingleCharDefinitionGeneratorProviderConfig();
    private final MultiCharDefinitionGeneratorProviderConfig multiCharDefinitionGeneratorConfig =
            new MultiCharDefinitionGeneratorProviderConfig();

    public AIProviderFactory() {}

    public ExampleProvider createExampleProvider(String providerName) {
        return createExampleProvider(providerName, null);
    }

    public ExampleProvider createExampleProvider(String providerName, String model) {
        if (providerName == null) providerName = "deepseek-chat";
        if (providerName.equals("openrouter") && (model == null || model.trim().isEmpty())) {
            model = "gpt-3.5-turbo";
        }

        return switch (providerName) {
            case "dummy" -> new DummyExampleProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Example> singleConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                singleCharExampleConfig.templatePath(),
                                singleCharExampleConfig.examplesDirectory(),
                                singleCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from DeepSeek (deepseek-chat)");
                ProviderConfig<Example> multiConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                multiCharExampleConfig.templatePath(),
                                multiCharExampleConfig.examplesDirectory(),
                                multiCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from DeepSeek (deepseek-chat)");
                yield new ConfigurableExampleProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "DeepSeek AI-powered example provider");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Example> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                singleCharExampleConfig.templatePath(),
                                singleCharExampleConfig.examplesDirectory(),
                                singleCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from Zhipu (glm-4-flash)");
                ProviderConfig<Example> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                multiCharExampleConfig.templatePath(),
                                multiCharExampleConfig.examplesDirectory(),
                                multiCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from Zhipu (glm-4-flash)");
                ZhipuChatModelProvider<Example> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Example> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExampleProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Example> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                singleCharExampleConfig.templatePath(),
                                singleCharExampleConfig.examplesDirectory(),
                                singleCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from Zhipu (glm-4.5)");
                ProviderConfig<Example> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                multiCharExampleConfig.templatePath(),
                                multiCharExampleConfig.examplesDirectory(),
                                multiCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from Zhipu (glm-4.5)");
                ZhipuChatModelProvider<Example> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Example> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExampleProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Example> singleConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                singleCharExampleConfig.templatePath(),
                                singleCharExampleConfig.examplesDirectory(),
                                singleCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from DashScope (" + providerName + ")");
                ProviderConfig<Example> multiConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                multiCharExampleConfig.templatePath(),
                                multiCharExampleConfig.examplesDirectory(),
                                multiCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from DashScope (" + providerName + ")");
                yield new ConfigurableExampleProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Qwen AI provider (" + providerName + ")");
            }
            case "openrouter" -> {
                requireAPIKey("OPENROUTER_API_KEY", providerName);
                ProviderConfig<Example> singleConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                singleCharExampleConfig.templatePath(),
                                singleCharExampleConfig.examplesDirectory(),
                                singleCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from OpenRouter (" + model + ")");
                ProviderConfig<Example> multiConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                multiCharExampleConfig.templatePath(),
                                multiCharExampleConfig.examplesDirectory(),
                                multiCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from OpenRouter (" + model + ")");
                yield new ConfigurableExampleProvider(
                        singleConfig, multiConfig, providerName, "OpenRouter AI (" + model + ")");
            }
            case "gemini-2.5-flash", "gemini-2.5-pro" -> {
                requireAPIKey("GEMINI_API_KEY", providerName);
                ProviderConfig<Example> singleConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null, // LangChain4j handles base URL internally
                                geminiConfig.getModelName(providerName),
                                singleCharExampleConfig.templatePath(),
                                singleCharExampleConfig.examplesDirectory(),
                                singleCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from Gemini (" + providerName + ")");
                ProviderConfig<Example> multiConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null,
                                geminiConfig.getModelName(providerName),
                                multiCharExampleConfig.templatePath(),
                                multiCharExampleConfig.examplesDirectory(),
                                multiCharExampleConfig.responseMapper(),
                                providerName,
                                "Failed to get examples from Gemini (" + providerName + ")");
                yield new ConfigurableExampleProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Gemini AI provider (" + providerName + ")");
            }
            default ->
                    throw new RuntimeException(
                            "Unknown example provider: "
                                    + providerName
                                    + ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro");
        };
    }

    public ExplanationProvider createExplanationProvider(String providerName) {
        return createExplanationProvider(providerName, null);
    }

    public ExplanationProvider createExplanationProvider(String providerName, String model) {
        if (providerName == null) providerName = "deepseek-chat";
        if (providerName.equals("openrouter") && (model == null || model.trim().isEmpty())) {
            model = "gpt-3.5-turbo";
        }

        return switch (providerName) {
            case "dummy" -> new DummyExplanationProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                singleCharExplanationConfig.templatePath(),
                                singleCharExplanationConfig.examplesDirectory(),
                                singleCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from DeepSeek (deepseek-chat)");
                ProviderConfig<Explanation> multiConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                multiCharExplanationConfig.templatePath(),
                                multiCharExplanationConfig.examplesDirectory(),
                                multiCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from DeepSeek (deepseek-chat)");
                yield new ConfigurableExplanationProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "DeepSeek AI-powered explanation provider");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                singleCharExplanationConfig.templatePath(),
                                singleCharExplanationConfig.examplesDirectory(),
                                singleCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from Zhipu (glm-4-flash)");
                ProviderConfig<Explanation> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                multiCharExplanationConfig.templatePath(),
                                multiCharExplanationConfig.examplesDirectory(),
                                multiCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from Zhipu (glm-4-flash)");
                ZhipuChatModelProvider<Explanation> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Explanation> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExplanationProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                singleCharExplanationConfig.templatePath(),
                                singleCharExplanationConfig.examplesDirectory(),
                                singleCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from Zhipu (glm-4.5)");
                ProviderConfig<Explanation> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                multiCharExplanationConfig.templatePath(),
                                multiCharExplanationConfig.examplesDirectory(),
                                multiCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from Zhipu (glm-4.5)");
                ZhipuChatModelProvider<Explanation> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Explanation> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableExplanationProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                singleCharExplanationConfig.templatePath(),
                                singleCharExplanationConfig.examplesDirectory(),
                                singleCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from DashScope (" + providerName + ")");
                ProviderConfig<Explanation> multiConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                multiCharExplanationConfig.templatePath(),
                                multiCharExplanationConfig.examplesDirectory(),
                                multiCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from DashScope (" + providerName + ")");
                yield new ConfigurableExplanationProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Qwen AI provider (" + providerName + ")");
            }
            case "openrouter" -> {
                requireAPIKey("OPENROUTER_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                singleCharExplanationConfig.templatePath(),
                                singleCharExplanationConfig.examplesDirectory(),
                                singleCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from OpenRouter (" + model + ")");
                ProviderConfig<Explanation> multiConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                multiCharExplanationConfig.templatePath(),
                                multiCharExplanationConfig.examplesDirectory(),
                                multiCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from OpenRouter (" + model + ")");
                yield new ConfigurableExplanationProvider(
                        singleConfig, multiConfig, providerName, "OpenRouter AI (" + model + ")");
            }
            case "gemini-2.5-flash", "gemini-2.5-pro" -> {
                requireAPIKey("GEMINI_API_KEY", providerName);
                ProviderConfig<Explanation> singleConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null, // LangChain4j handles base URL internally
                                geminiConfig.getModelName(providerName),
                                singleCharExplanationConfig.templatePath(),
                                singleCharExplanationConfig.examplesDirectory(),
                                singleCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from Gemini (" + providerName + ")");
                ProviderConfig<Explanation> multiConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null,
                                geminiConfig.getModelName(providerName),
                                multiCharExplanationConfig.templatePath(),
                                multiCharExplanationConfig.examplesDirectory(),
                                multiCharExplanationConfig.responseMapper(),
                                providerName,
                                "Failed to get explanation from Gemini (" + providerName + ")");
                yield new ConfigurableExplanationProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Gemini AI provider (" + providerName + ")");
            }
            default ->
                    throw new RuntimeException(
                            "Unknown explanation provider: "
                                    + providerName
                                    + ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro");
        };
    }

    public StructuralDecompositionProvider createDecompositionProvider(String providerName) {
        return createDecompositionProvider(providerName, null);
    }

    public StructuralDecompositionProvider createDecompositionProvider(
            String providerName, String model) {
        if (providerName == null) providerName = "deepseek-chat";
        if (providerName.equals("openrouter") && (model == null || model.trim().isEmpty())) {
            model = "gpt-3.5-turbo";
        }

        return switch (providerName) {
            case "dummy" -> new DummyStructuralDecompositionProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                singleCharStructuralConfig.templatePath(),
                                singleCharStructuralConfig.examplesDirectory(),
                                singleCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from DeepSeek (deepseek-chat)");
                ProviderConfig<StructuralDecomposition> multiConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                multiCharStructuralConfig.templatePath(),
                                multiCharStructuralConfig.examplesDirectory(),
                                multiCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from DeepSeek (deepseek-chat)");
                yield new ConfigurableStructuralDecompositionProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "DeepSeek AI-powered structural decomposition provider");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                singleCharStructuralConfig.templatePath(),
                                singleCharStructuralConfig.examplesDirectory(),
                                singleCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from Zhipu (glm-4-flash)");
                ProviderConfig<StructuralDecomposition> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                multiCharStructuralConfig.templatePath(),
                                multiCharStructuralConfig.examplesDirectory(),
                                multiCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from Zhipu (glm-4-flash)");
                ZhipuChatModelProvider<StructuralDecomposition> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<StructuralDecomposition> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableStructuralDecompositionProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4 Flash AI provider");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                singleCharStructuralConfig.templatePath(),
                                singleCharStructuralConfig.examplesDirectory(),
                                singleCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from Zhipu (glm-4.5)");
                ProviderConfig<StructuralDecomposition> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                multiCharStructuralConfig.templatePath(),
                                multiCharStructuralConfig.examplesDirectory(),
                                multiCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from Zhipu (glm-4.5)");
                ZhipuChatModelProvider<StructuralDecomposition> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<StructuralDecomposition> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableStructuralDecompositionProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4.5 AI provider");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                singleCharStructuralConfig.templatePath(),
                                singleCharStructuralConfig.examplesDirectory(),
                                singleCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from DashScope ("
                                        + providerName
                                        + ")");
                ProviderConfig<StructuralDecomposition> multiConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                multiCharStructuralConfig.templatePath(),
                                multiCharStructuralConfig.examplesDirectory(),
                                multiCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from DashScope ("
                                        + providerName
                                        + ")");
                yield new ConfigurableStructuralDecompositionProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Qwen AI provider (" + providerName + ")");
            }
            case "openrouter" -> {
                requireAPIKey("OPENROUTER_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                singleCharStructuralConfig.templatePath(),
                                singleCharStructuralConfig.examplesDirectory(),
                                singleCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from OpenRouter ("
                                        + model
                                        + ")");
                ProviderConfig<StructuralDecomposition> multiConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                multiCharStructuralConfig.templatePath(),
                                multiCharStructuralConfig.examplesDirectory(),
                                multiCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from OpenRouter ("
                                        + model
                                        + ")");
                yield new ConfigurableStructuralDecompositionProvider(
                        singleConfig, multiConfig, providerName, "OpenRouter AI (" + model + ")");
            }
            case "gemini-2.5-flash", "gemini-2.5-pro" -> {
                requireAPIKey("GEMINI_API_KEY", providerName);
                ProviderConfig<StructuralDecomposition> singleConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null, // LangChain4j handles base URL internally
                                geminiConfig.getModelName(providerName),
                                singleCharStructuralConfig.templatePath(),
                                singleCharStructuralConfig.examplesDirectory(),
                                singleCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from Gemini ("
                                        + providerName
                                        + ")");
                ProviderConfig<StructuralDecomposition> multiConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null,
                                geminiConfig.getModelName(providerName),
                                multiCharStructuralConfig.templatePath(),
                                multiCharStructuralConfig.examplesDirectory(),
                                multiCharStructuralConfig.responseMapper(),
                                providerName,
                                "Failed to get structural decomposition from Gemini ("
                                        + providerName
                                        + ")");
                yield new ConfigurableStructuralDecompositionProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Gemini AI provider (" + providerName + ")");
            }
            default ->
                    throw new RuntimeException(
                            "Unknown decomposition provider: "
                                    + providerName
                                    + ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro");
        };
    }

    private <T> ProviderConfig<T> createProviderConfig(
            String apiKey,
            String baseUrl,
            String modelName,
            String templateResourcePath,
            String examplesResourcePath,
            Function<String, T> responseMapper,
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
                errorMessagePrefix);
    }

    private void requireAPIKey(String keyName, String providerName) {
        String key = readEnv(keyName);
        if (key == null || key.trim().isEmpty()) {
            throw new RuntimeException(
                    "Provider '"
                            + providerName
                            + "' requires "
                            + keyName
                            + " environment variable to be set");
        }
    }

    private String readEnv(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    public DefinitionFormatterProvider createDefinitionFormatterProvider(String providerName) {
        return createDefinitionFormatterProvider(providerName, null);
    }

    public DefinitionFormatterProvider createDefinitionFormatterProvider(
            String providerName, String model) {
        if (providerName == null) providerName = "deepseek-chat";
        if (providerName.equals("openrouter") && (model == null || model.trim().isEmpty())) {
            model = "gpt-3.5-turbo";
        }

        return switch (providerName) {
            case "dummy" -> new DummyDefinitionFormatterProvider();
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                singleCharDefinitionConfig.templatePath(),
                                singleCharDefinitionConfig.examplesDirectory(),
                                singleCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from DeepSeek (deepseek-chat)");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                multiCharDefinitionConfig.templatePath(),
                                multiCharDefinitionConfig.examplesDirectory(),
                                multiCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from DeepSeek (deepseek-chat)");
                yield new ConfigurableDefinitionFormatterProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "DeepSeek AI-powered definition formatter");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                singleCharDefinitionConfig.templatePath(),
                                singleCharDefinitionConfig.examplesDirectory(),
                                singleCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from Zhipu (glm-4-flash)");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                multiCharDefinitionConfig.templatePath(),
                                multiCharDefinitionConfig.examplesDirectory(),
                                multiCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from Zhipu (glm-4-flash)");
                ZhipuChatModelProvider<Definition> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Definition> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableDefinitionFormatterProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4 Flash AI definition formatter");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                singleCharDefinitionConfig.templatePath(),
                                singleCharDefinitionConfig.examplesDirectory(),
                                singleCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from Zhipu (glm-4.5)");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                multiCharDefinitionConfig.templatePath(),
                                multiCharDefinitionConfig.examplesDirectory(),
                                multiCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from Zhipu (glm-4.5)");
                ZhipuChatModelProvider<Definition> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Definition> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableDefinitionFormatterProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4.5 AI definition formatter");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                singleCharDefinitionConfig.templatePath(),
                                singleCharDefinitionConfig.examplesDirectory(),
                                singleCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from DashScope ("
                                        + providerName
                                        + ")");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                multiCharDefinitionConfig.templatePath(),
                                multiCharDefinitionConfig.examplesDirectory(),
                                multiCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from DashScope ("
                                        + providerName
                                        + ")");
                yield new ConfigurableDefinitionFormatterProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Qwen AI definition formatter (" + providerName + ")");
            }
            case "openrouter" -> {
                requireAPIKey("OPENROUTER_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                singleCharDefinitionConfig.templatePath(),
                                singleCharDefinitionConfig.examplesDirectory(),
                                singleCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from OpenRouter (" + model + ")");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                multiCharDefinitionConfig.templatePath(),
                                multiCharDefinitionConfig.examplesDirectory(),
                                multiCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from OpenRouter (" + model + ")");
                yield new ConfigurableDefinitionFormatterProvider(
                        singleConfig, multiConfig, providerName, "OpenRouter AI (" + model + ")");
            }
            case "gemini-2.5-flash", "gemini-2.5-pro" -> {
                requireAPIKey("GEMINI_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null, // LangChain4j handles base URL internally
                                geminiConfig.getModelName(providerName),
                                singleCharDefinitionConfig.templatePath(),
                                singleCharDefinitionConfig.examplesDirectory(),
                                singleCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from Gemini (" + providerName + ")");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null,
                                geminiConfig.getModelName(providerName),
                                multiCharDefinitionConfig.templatePath(),
                                multiCharDefinitionConfig.examplesDirectory(),
                                multiCharDefinitionConfig.responseMapper(),
                                providerName,
                                "Failed to format definition from Gemini (" + providerName + ")");
                yield new ConfigurableDefinitionFormatterProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Gemini AI provider (" + providerName + ")");
            }
            default ->
                    throw new RuntimeException(
                            "Unknown definition formatter provider: "
                                    + providerName
                                    + ". Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro");
        };
    }

    public DefinitionGeneratorProvider createDefinitionGeneratorProvider(String providerName) {
        return createDefinitionGeneratorProvider(providerName, null);
    }

    public DefinitionGeneratorProvider createDefinitionGeneratorProvider(
            String providerName, String model) {
        if (providerName == null) providerName = "deepseek-chat";
        if (providerName.equals("openrouter") && (model == null || model.trim().isEmpty())) {
            model = "gpt-3.5-turbo";
        }

        return switch (providerName) {
            case "deepseek-chat" -> {
                requireAPIKey("DEEPSEEK_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                singleCharDefinitionGeneratorConfig.templatePath(),
                                singleCharDefinitionGeneratorConfig.examplesDirectory(),
                                singleCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from DeepSeek (deepseek-chat)");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                deepSeekConfig.getApiKey(),
                                deepSeekConfig.getBaseUrl(),
                                "deepseek-chat",
                                multiCharDefinitionGeneratorConfig.templatePath(),
                                multiCharDefinitionGeneratorConfig.examplesDirectory(),
                                multiCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from DeepSeek (deepseek-chat)");
                yield new ConfigurableDefinitionGeneratorProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "DeepSeek AI-powered definition generator");
            }
            case "glm-4-flash" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                singleCharDefinitionGeneratorConfig.templatePath(),
                                singleCharDefinitionGeneratorConfig.examplesDirectory(),
                                singleCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from Zhipu (glm-4-flash)");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4-flash",
                                multiCharDefinitionGeneratorConfig.templatePath(),
                                multiCharDefinitionGeneratorConfig.examplesDirectory(),
                                multiCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from Zhipu (glm-4-flash)");
                ZhipuChatModelProvider<Definition> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Definition> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableDefinitionGeneratorProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4 Flash AI definition generator");
            }
            case "glm-4.5" -> {
                requireAPIKey("ZHIPU_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                singleCharDefinitionGeneratorConfig.templatePath(),
                                singleCharDefinitionGeneratorConfig.examplesDirectory(),
                                singleCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from Zhipu (glm-4.5)");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                zhipuConfig.getApiKey(),
                                zhipuConfig.getBaseUrl(),
                                "glm-4.5",
                                multiCharDefinitionGeneratorConfig.templatePath(),
                                multiCharDefinitionGeneratorConfig.examplesDirectory(),
                                multiCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from Zhipu (glm-4.5)");
                ZhipuChatModelProvider<Definition> singleDelegate =
                        new ZhipuChatModelProvider<>(singleConfig);
                ZhipuChatModelProvider<Definition> multiDelegate =
                        new ZhipuChatModelProvider<>(multiConfig);
                yield new ConfigurableDefinitionGeneratorProvider(
                        singleDelegate::process,
                        multiDelegate::process,
                        providerName,
                        "GLM-4.5 AI definition generator");
            }
            case "qwen-max", "qwen-plus", "qwen-turbo" -> {
                requireAPIKey("DASHSCOPE_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                singleCharDefinitionGeneratorConfig.templatePath(),
                                singleCharDefinitionGeneratorConfig.examplesDirectory(),
                                singleCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from DashScope ("
                                        + providerName
                                        + ")");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                dashScopeConfig.getApiKey(),
                                dashScopeConfig.getBaseUrl(),
                                providerName,
                                multiCharDefinitionGeneratorConfig.templatePath(),
                                multiCharDefinitionGeneratorConfig.examplesDirectory(),
                                multiCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from DashScope ("
                                        + providerName
                                        + ")");
                yield new ConfigurableDefinitionGeneratorProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Qwen AI definition generator (" + providerName + ")");
            }
            case "openrouter" -> {
                requireAPIKey("OPENROUTER_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                singleCharDefinitionGeneratorConfig.templatePath(),
                                singleCharDefinitionGeneratorConfig.examplesDirectory(),
                                singleCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from OpenRouter (" + model + ")");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                openRouterConfig.getApiKey(),
                                openRouterConfig.getBaseUrl(),
                                model,
                                multiCharDefinitionGeneratorConfig.templatePath(),
                                multiCharDefinitionGeneratorConfig.examplesDirectory(),
                                multiCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from OpenRouter (" + model + ")");
                yield new ConfigurableDefinitionGeneratorProvider(
                        singleConfig, multiConfig, providerName, "OpenRouter AI (" + model + ")");
            }
            case "gemini-2.5-flash", "gemini-2.5-pro" -> {
                requireAPIKey("GEMINI_API_KEY", providerName);
                ProviderConfig<Definition> singleConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null, // LangChain4j handles base URL internally
                                geminiConfig.getModelName(providerName),
                                singleCharDefinitionGeneratorConfig.templatePath(),
                                singleCharDefinitionGeneratorConfig.examplesDirectory(),
                                singleCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from Gemini (" + providerName + ")");
                ProviderConfig<Definition> multiConfig =
                        createProviderConfig(
                                geminiConfig.getApiKey(),
                                null,
                                geminiConfig.getModelName(providerName),
                                multiCharDefinitionGeneratorConfig.templatePath(),
                                multiCharDefinitionGeneratorConfig.examplesDirectory(),
                                multiCharDefinitionGeneratorConfig.responseMapper(),
                                providerName,
                                "Failed to generate definition from Gemini (" + providerName + ")");
                yield new ConfigurableDefinitionGeneratorProvider(
                        singleConfig,
                        multiConfig,
                        providerName,
                        "Gemini AI provider (" + providerName + ")");
            }
            default ->
                    throw new RuntimeException(
                            "Unknown definition generator provider: "
                                    + providerName
                                    + ". Available: deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro");
        };
    }
}
