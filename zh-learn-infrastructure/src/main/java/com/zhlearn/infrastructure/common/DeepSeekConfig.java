package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

public class DeepSeekConfig {

    public static final String BASE_URL = "https://api.deepseek.com/v1";
    public static final String MODEL_NAME = "deepseek-chat";
    public static final String API_KEY_ENVIRONMENT_VARIABLE = "DEEPSEEK_API_KEY";
    public static final String BASE_URL_ENVIRONMENT_VARIABLE = "DEEPSEEK_BASE_URL";

    public static ProviderConfig<Explanation> forExplanation() {
        return forExplanation(readKey(API_KEY_ENVIRONMENT_VARIABLE), readKey(BASE_URL_ENVIRONMENT_VARIABLE, BASE_URL), MODEL_NAME);
    }
    
    public static ProviderConfig<Explanation> forExplanation(String apiKey) {
        return forExplanation(apiKey, readKey(BASE_URL_ENVIRONMENT_VARIABLE, BASE_URL), MODEL_NAME);
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
            "Failed to get explanation from DeepSeek API"
        );
    }
    
    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition() {
        return forStructuralDecomposition(readKey(API_KEY_ENVIRONMENT_VARIABLE), readKey(BASE_URL_ENVIRONMENT_VARIABLE, BASE_URL), MODEL_NAME);
    }
    
    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String apiKey) {
        return forStructuralDecomposition(apiKey, readKey(BASE_URL_ENVIRONMENT_VARIABLE, BASE_URL), MODEL_NAME);
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
            "Failed to get structural decomposition from DeepSeek API"
        );
    }
    
    public static ProviderConfig<Example> forExamples() {
        return forExamples(readKey(API_KEY_ENVIRONMENT_VARIABLE), readKey(BASE_URL_ENVIRONMENT_VARIABLE, BASE_URL), MODEL_NAME);
    }
    
    public static ProviderConfig<Example> forExamples(String apiKey) {
        return forExamples(apiKey, readKey(BASE_URL_ENVIRONMENT_VARIABLE, BASE_URL), MODEL_NAME);
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
            "Failed to get examples from DeepSeek API"
        );
    }
}
