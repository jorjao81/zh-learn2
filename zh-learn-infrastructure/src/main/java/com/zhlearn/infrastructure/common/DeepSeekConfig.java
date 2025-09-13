package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

public class DeepSeekConfig {

    public static final String BASE_URL = "https://api.deepseek.com/v1";
    public static final String MODEL_NAME = "deepseek-chat";
    public static final String API_KEY_ENVIRONMENT_VARIABLE = "DEEPSEEK_API_KEY";

    public static ProviderConfig<Explanation> forExplanation() {
        return forExplanation(System.getenv(API_KEY_ENVIRONMENT_VARIABLE), BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<Explanation> forExplanation(String apiKey) {
        return forExplanation(apiKey, BASE_URL, MODEL_NAME);
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
        return forStructuralDecomposition(System.getenv(API_KEY_ENVIRONMENT_VARIABLE), BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String apiKey) {
        return forStructuralDecomposition(apiKey, BASE_URL, MODEL_NAME);
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
        return forExamples(System.getenv(API_KEY_ENVIRONMENT_VARIABLE), BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<Example> forExamples(String apiKey) {
        return forExamples(apiKey, BASE_URL, MODEL_NAME);
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
