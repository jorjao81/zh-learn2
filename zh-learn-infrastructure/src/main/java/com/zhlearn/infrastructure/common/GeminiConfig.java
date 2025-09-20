package com.zhlearn.infrastructure.common;

/**
 * Configuration factory for Google Gemini models via LangChain4j.
 */
public final class GeminiConfig {

    private static final String API_KEY_ENVIRONMENT_VARIABLE = "GEMINI_API_KEY";

    private GeminiConfig() {}

    public static String getApiKey() {
        return readKey(API_KEY_ENVIRONMENT_VARIABLE);
    }

    public static String getModelName(String providerName) {
        return switch (providerName) {
            case "gemini-2.5-flash" -> "gemini-2.5-flash";
            case "gemini-2.5-pro" -> "gemini-2.5-pro";
            default -> throw new IllegalArgumentException("Unknown Gemini model: " + providerName);
        };
    }

    private static String readKey(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }
}