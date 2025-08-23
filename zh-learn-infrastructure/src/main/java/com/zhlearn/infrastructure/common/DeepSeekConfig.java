package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

public class DeepSeekConfig {
    
    public static ProviderConfig<Explanation> forExplanation() {
        return forExplanation(System.getenv("DEEPSEEK_API_KEY"), "https://api.deepseek.com", "deepseek-chat");
    }
    
    public static ProviderConfig<Explanation> forExplanation(String apiKey) {
        return forExplanation(apiKey, "https://api.deepseek.com", "deepseek-chat");
    }
    
    public static ProviderConfig<Explanation> forExplanation(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            0.3, // temperature
            1000, // maxTokens
            "/explanation/prompt-template.md",
            "/explanation/examples/",
            Explanation::new,
            modelName,
            "Failed to get explanation from DeepSeek API"
        );
    }
    
    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition() {
        return forStructuralDecomposition(System.getenv("DEEPSEEK_API_KEY"), "https://api.deepseek.com", "deepseek-chat");
    }
    
    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String apiKey) {
        return forStructuralDecomposition(apiKey, "https://api.deepseek.com", "deepseek-chat");
    }
    
    public static ProviderConfig<StructuralDecomposition> forStructuralDecomposition(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            0.3, // temperature
            1000, // maxTokens
            "/structural-decomposition/prompt-template.md",
            "/structural-decomposition/examples/",
            StructuralDecomposition::new,
            modelName,
            "Failed to get structural decomposition from DeepSeek API"
        );
    }
    
    public static ProviderConfig<Example> forExamples() {
        return forExamples(System.getenv("DEEPSEEK_API_KEY"), "https://api.deepseek.com", "deepseek-chat");
    }
    
    public static ProviderConfig<Example> forExamples(String apiKey) {
        return forExamples(apiKey, "https://api.deepseek.com", "deepseek-chat");
    }
    
    public static ProviderConfig<Example> forExamples(String apiKey, String baseUrl, String modelName) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            0.3, // temperature
            1000, // maxTokens
            "/examples/prompt-template.md",
            "/examples/examples/",
            new ExampleResponseMapper(),
            modelName,
            "Failed to get examples from DeepSeek API"
        );
    }
}