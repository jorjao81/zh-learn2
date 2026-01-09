package com.zhlearn.infrastructure.qwen;

/**
 * Configuration for DashScope (Alibaba Cloud) API used by Qwen TTS.
 *
 * <p>Environment variables:
 *
 * <ul>
 *   <li>{@code DASHSCOPE_API_KEY} - Required API key from Alibaba Cloud DashScope
 *   <li>{@code DASHSCOPE_BASE_URL} - Optional base URL override (defaults to international
 *       endpoint)
 * </ul>
 */
public final class DashScopeConfig {

    public static final String API_KEY_ENV = "DASHSCOPE_API_KEY";
    public static final String BASE_URL_ENV = "DASHSCOPE_BASE_URL";
    public static final String DEFAULT_BASE_URL = "https://dashscope-intl.aliyuncs.com";
    public static final String TTS_ENDPOINT_PATH =
            "/api/v1/services/aigc/multimodal-generation/generation";

    private DashScopeConfig() {}

    public static String getApiKey() {
        String key = System.getenv(API_KEY_ENV);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    API_KEY_ENV
                            + " environment variable is required for DashScope/Qwen TTS provider");
        }
        return key;
    }

    public static String getBaseUrl() {
        String url = System.getenv(BASE_URL_ENV);
        return (url != null && !url.isBlank()) ? url : DEFAULT_BASE_URL;
    }

    public static String getTtsEndpoint() {
        return getBaseUrl() + TTS_ENDPOINT_PATH;
    }
}
