package com.zhlearn.infrastructure.common;

/**
 * Configuration for DeepSeek providers (OpenAI-compatible endpoint).
 * Singleton service that reads configuration from environment variables.
 */
public class DeepSeekConfig {

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/v1";
    private static final String API_KEY_ENVIRONMENT_VARIABLE = "DEEPSEEK_API_KEY";
    private static final String BASE_URL_ENVIRONMENT_VARIABLE = "DEEPSEEK_BASE_URL";

    private final String apiKey;
    private final String baseUrl;

    public DeepSeekConfig() {
        this.apiKey = readKey(API_KEY_ENVIRONMENT_VARIABLE);
        this.baseUrl = readKey(BASE_URL_ENVIRONMENT_VARIABLE, DEFAULT_BASE_URL);
    }

    private String readKey(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    private String readKey(String key, String defaultValue) {
        String value = readKey(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
