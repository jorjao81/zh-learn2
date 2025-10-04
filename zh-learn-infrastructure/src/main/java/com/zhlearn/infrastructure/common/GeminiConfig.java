package com.zhlearn.infrastructure.common;

/**
 * Configuration for Google Gemini models via LangChain4j.
 * Singleton service that reads configuration from environment variables.
 */
public class GeminiConfig {

    private static final String API_KEY_ENVIRONMENT_VARIABLE = "GEMINI_API_KEY";

    private final String apiKey;

    public GeminiConfig() {
        this.apiKey = readKey(API_KEY_ENVIRONMENT_VARIABLE);
    }

    private String readKey(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModelName(String providerName) {
        return switch (providerName) {
            case "gemini-2.5-flash" -> "gemini-2.5-flash";
            case "gemini-2.5-pro" -> "gemini-2.5-pro";
            default -> throw new IllegalArgumentException("Unknown Gemini model: " + providerName);
        };
    }
}