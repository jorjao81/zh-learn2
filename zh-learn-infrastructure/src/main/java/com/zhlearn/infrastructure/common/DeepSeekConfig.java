package com.zhlearn.infrastructure.common;

/**
 * Configuration factory for DeepSeek providers (OpenAI-compatible endpoint).
 */
public final class DeepSeekConfig {

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/v1";
    private static final String API_KEY_ENVIRONMENT_VARIABLE = "DEEPSEEK_API_KEY";
    private static final String BASE_URL_ENVIRONMENT_VARIABLE = "DEEPSEEK_BASE_URL";

    private DeepSeekConfig() {
    }

    private static String readKey(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    private static String readKey(String key, String defaultValue) {
        String value = readKey(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static String getApiKey() {
        return readKey(API_KEY_ENVIRONMENT_VARIABLE);
    }

    public static String getBaseUrl() {
        return readKey(BASE_URL_ENVIRONMENT_VARIABLE, DEFAULT_BASE_URL);
    }
}
