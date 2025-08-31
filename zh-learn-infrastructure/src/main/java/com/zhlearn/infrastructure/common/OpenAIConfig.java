package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

public class OpenAIConfig {

    public static final String BASE_URL = "https://api.openai.com/v1";
    public static final String MODEL_NAME = "gpt-5-nano";
    public static final String API_KEY_ENVIRONMENT_VARIABLE = "OPENAI_API_KEY";

    public static ProviderConfig<Explanation> forGPT5NanoExplanation() {
        return forGPT5NanoExplanation(System.getenv(API_KEY_ENVIRONMENT_VARIABLE), BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<Explanation> forGPT5NanoExplanation(String apiKey) {
        return forGPT5NanoExplanation(apiKey, BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<Explanation> forGPT5NanoExplanation(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            null, // no temperature for GPT-5 Nano
            null, // no maxTokens for GPT-5 Nano
            "/explanation/prompt-template.md",
            "/explanation/examples/",
            Explanation::new,
            modelName,
            "Failed to get explanation from GPT-5 Nano API"
        );
    }
    
    public static ProviderConfig<StructuralDecomposition> forGPT5NanoStructuralDecomposition() {
        return forGPT5NanoStructuralDecomposition(System.getenv(API_KEY_ENVIRONMENT_VARIABLE), BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<StructuralDecomposition> forGPT5NanoStructuralDecomposition(String apiKey) {
        return forGPT5NanoStructuralDecomposition(apiKey, BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<StructuralDecomposition> forGPT5NanoStructuralDecomposition(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            null, // no temperature for GPT-5 Nano
            null, // no maxTokens for GPT-5 Nano
            "/structural-decomposition/prompt-template.md",
            "/structural-decomposition/examples/",
            StructuralDecomposition::new,
            modelName,
            "Failed to get structural decomposition from GPT-5 Nano API"
        );
    }
    
    public static ProviderConfig<Example> forGPT5NanoExamples() {
        return forGPT5NanoExamples(System.getenv(API_KEY_ENVIRONMENT_VARIABLE), BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<Example> forGPT5NanoExamples(String apiKey) {
        return forGPT5NanoExamples(apiKey, BASE_URL, MODEL_NAME);
    }
    
    public static ProviderConfig<Example> forGPT5NanoExamples(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            null, // no temperature for GPT-5 Nano
            null, // no maxTokens for GPT-5 Nano
            "/examples/prompt-template.md",
            "/examples/examples/",
            new ExampleResponseMapper(),
            modelName,
            "Failed to get examples from GPT-5 Nano API"
        );
    }
}