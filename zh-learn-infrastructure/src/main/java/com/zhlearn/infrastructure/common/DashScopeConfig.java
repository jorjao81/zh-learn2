package com.zhlearn.infrastructure.common;

/**
 * Configuration factory for Alibaba Cloud DashScope (Qwen3) via OpenAI-compatible endpoint.
 * Uses DASHSCOPE_API_KEY and optional DASHSCOPE_BASE_URL (defaults to China Mainland endpoint).
 */
public class DashScopeConfig {

    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    public static final String API_KEY_ENVIRONMENT_VARIABLE = "DASHSCOPE_API_KEY";
    public static final String BASE_URL_ENVIRONMENT_VARIABLE = "DASHSCOPE_BASE_URL";

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

    public static String getApiKey() {
        return readKey(API_KEY_ENVIRONMENT_VARIABLE);
    }

    public static String getBaseUrl() {
        return readKey(BASE_URL_ENVIRONMENT_VARIABLE, DEFAULT_BASE_URL);
    }
}
