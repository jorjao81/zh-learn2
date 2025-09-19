package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

/**
 * Configuration factory for Alibaba Cloud DashScope (Qwen3) via OpenAI-compatible endpoint.
 * Uses DASHSCOPE_API_KEY and optional DASHSCOPE_BASE_URL (defaults to China Mainland endpoint).
 */
public class DashScopeConfig {

    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    public static final String API_KEY_ENVIRONMENT_VARIABLE = "DASHSCOPE_API_KEY";
    public static final String BASE_URL_ENVIRONMENT_VARIABLE = "DASHSCOPE_BASE_URL";

    private static String readKey(String key) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        return v;
    }

    private static String readKey(String key, String defaultValue) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }

    public static ProviderConfig<Explanation> forExplanation(String modelName) {
        return forExplanation(modelName, modelName);
    }

    public static ProviderConfig<Explanation> forExplanation(String providerName, String modelName) {
        return new ProviderConfig<>(
            readKey(API_KEY_ENVIRONMENT_VARIABLE),
            readKey(BASE_URL_ENVIRONMENT_VARIABLE, DEFAULT_BASE_URL),
            modelName,
            0.3, // temperature
            8000, // maxTokens
            "/explanation/prompt-template.md",
            "/explanation/examples/",
            Explanation::new,
            providerName,
            "Failed to get explanation from DashScope (" + modelName + ")"
        );
    }

    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String modelName) {
        return forStructuralDecomposition(modelName, modelName);
    }

    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String providerName, String modelName) {
        return new ProviderConfig<>(
            readKey(API_KEY_ENVIRONMENT_VARIABLE),
            readKey(BASE_URL_ENVIRONMENT_VARIABLE, DEFAULT_BASE_URL),
            modelName,
            0.3, // temperature
            8000, // maxTokens
            "/structural-decomposition/prompt-template.md",
            "/structural-decomposition/examples/",
            StructuralDecomposition::new,
            providerName,
            "Failed to get structural decomposition from DashScope (" + modelName + ")"
        );
    }

    public static ProviderConfig<Example> forExamples(String modelName) {
        return forExamples(modelName, modelName);
    }

    public static ProviderConfig<Example> forExamples(String providerName, String modelName) {
        return new ProviderConfig<>(
            readKey(API_KEY_ENVIRONMENT_VARIABLE),
            readKey(BASE_URL_ENVIRONMENT_VARIABLE, DEFAULT_BASE_URL),
            modelName,
            0.3, // temperature
            8000, // maxTokens
            "/examples/prompt-template.md",
            "/examples/examples/",
            new ExampleResponseMapper(),
            providerName,
            "Failed to get examples from DashScope (" + modelName + ")"
        );
    }
}