package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

/**
 * Configuration for ChatGLM via z.ai (OpenAI-compatible) endpoints.
 *
 * Defaults assume an OpenAI-compatible base URL. Override via env/system properties:
 *  - ZAI_API_KEY
 *  - ZAI_BASE_URL (e.g., https://api.z.ai/v1 or provider-specific gateway)
 */
public class ChatGLMConfig {

    public static final String DEFAULT_BASE_URL = "https://api.z.ai/openai/v1"; // OpenAI-compatible path per z.ai docs
    public static final String DEFAULT_MODEL_NAME = "glm-4-flash";

    public static final String API_KEY_ENVIRONMENT_VARIABLE = "ZAI_API_KEY";
    public static final String API_KEY_ENVIRONMENT_VARIABLE_FALLBACK = "CHAT_GLM_API_KEY"; // backward-compat
    public static final String BASE_URL_ENVIRONMENT_VARIABLE = "ZAI_BASE_URL";
    public static final String BASE_URL_ENVIRONMENT_VARIABLE_FALLBACK = "CHAT_GLM_BASE_URL"; // backward-compat
    public static final String MODEL_ENVIRONMENT_VARIABLE = "ZAI_MODEL";
    public static final String MODEL_ENVIRONMENT_VARIABLE_FALLBACK = "CHAT_GLM_MODEL"; // backward-compat

    public static ProviderConfig<Explanation> forExplanation() {
        return forExplanation(readApiKey(),
                readBaseUrl(),
                readModelName());
    }

    public static ProviderConfig<Explanation> forExplanation(String apiKey) {
        return forExplanation(apiKey,
                readBaseUrl(),
                readModelName());
    }

    public static ProviderConfig<Explanation> forExplanation(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            0.3, // temperature
            8000, // maxTokens
            "/explanation/prompt-template.md",
            "/explanation/examples/",
            Explanation::new,
            modelName,
            "Failed to get explanation from ChatGLM (z.ai) API"
        );
    }

    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition() {
        return forStructuralDecomposition(readApiKey(),
                readBaseUrl(),
                readModelName());
    }

    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String apiKey) {
        return forStructuralDecomposition(apiKey,
                readBaseUrl(),
                readModelName());
    }

    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            0.3, // temperature
            8000, // maxTokens
            "/structural-decomposition/prompt-template.md",
            "/structural-decomposition/examples/",
            StructuralDecomposition::new,
            modelName,
            "Failed to get structural decomposition from ChatGLM (z.ai) API"
        );
    }

    public static ProviderConfig<Example> forExamples() {
        return forExamples(readApiKey(),
                readBaseUrl(),
                readModelName());
    }

    public static ProviderConfig<Example> forExamples(String apiKey) {
        return forExamples(apiKey,
                readBaseUrl(),
                readModelName());
    }

    public static ProviderConfig<Example> forExamples(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            0.3, // temperature
            8000, // maxTokens
            "/examples/prompt-template.md",
            "/examples/examples/",
            new ExampleResponseMapper(),
            modelName,
            "Failed to get examples from ChatGLM (z.ai) API"
        );
    }

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

    private static String readApiKey() {
        String v = readKey(API_KEY_ENVIRONMENT_VARIABLE);
        if (v == null || v.isBlank()) v = readKey(API_KEY_ENVIRONMENT_VARIABLE_FALLBACK);
        return v;
    }

    private static String readBaseUrl() {
        String v = readKey(BASE_URL_ENVIRONMENT_VARIABLE);
        if (v == null || v.isBlank()) v = readKey(BASE_URL_ENVIRONMENT_VARIABLE_FALLBACK);
        return (v == null || v.isBlank()) ? DEFAULT_BASE_URL : v;
    }

    private static String readModelName() {
        String v = readKey(MODEL_ENVIRONMENT_VARIABLE);
        if (v == null || v.isBlank()) v = readKey(MODEL_ENVIRONMENT_VARIABLE_FALLBACK);
        return (v == null || v.isBlank()) ? DEFAULT_MODEL_NAME : v;
    }
}
