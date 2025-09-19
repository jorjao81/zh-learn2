package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

import java.util.function.Function;

/**
 * Simplified configuration record for providers.
 * Replaces the complex factory methods and configuration classes.
 */
public record SimpleProviderConfig(
    String name,
    String description,
    String baseUrl,
    String modelName,
    String apiKey,
    Double temperature,
    Integer maxTokens
) {

    /**
     * Convert to internal ProviderConfig that GenericChatModelProvider expects
     */
    public <T> ProviderConfig<T> toInternalConfig(Class<T> type) {
        return new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            temperature,
            maxTokens,
            getTemplateResourcePath(type),
            getExamplesResourcePath(type),
            getResponseMapper(type),
            name,
            getErrorMessagePrefix()
        );
    }

    private <T> String getTemplateResourcePath(Class<T> type) {
        if (type == Example.class) {
            return "/examples/prompt-template.md";
        } else if (type == Explanation.class) {
            return "/explanation/prompt-template.md";
        } else if (type == StructuralDecomposition.class) {
            return "/structural-decomposition/prompt-template.md";
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    private <T> String getExamplesResourcePath(Class<T> type) {
        if (type == Example.class) {
            return "/examples/examples/";
        } else if (type == Explanation.class) {
            return "/explanation/examples/";
        } else if (type == StructuralDecomposition.class) {
            return "/structural-decomposition/examples/";
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    @SuppressWarnings("unchecked")
    private <T> Function<String, T> getResponseMapper(Class<T> type) {
        if (type == Example.class) {
            return (Function<String, T>) new ExampleResponseMapper();
        } else if (type == Explanation.class) {
            return (Function<String, T>) (Function<String, Explanation>) Explanation::new;
        } else if (type == StructuralDecomposition.class) {
            return (Function<String, T>) (Function<String, StructuralDecomposition>) StructuralDecomposition::new;
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    private String getErrorMessagePrefix() {
        return "Failed to get response from " + name + " API";
    }

    /**
     * Helper method to read environment variables or system properties
     */
    public static String readEnv(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    /**
     * Helper method to read environment variables with fallback
     */
    public static String readEnv(String key, String defaultValue) {
        String value = readEnv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}