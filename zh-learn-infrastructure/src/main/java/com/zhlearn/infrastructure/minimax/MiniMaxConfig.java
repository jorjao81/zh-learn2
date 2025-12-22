package com.zhlearn.infrastructure.minimax;

/**
 * Configuration for MiniMax TTS API. Requires both an API key and a Group ID for authentication.
 *
 * <p>Environment variables:
 *
 * <ul>
 *   <li>{@code MINIMAX_API_KEY} - Required API key from MiniMax platform
 *   <li>{@code MINIMAX_GROUP_ID} - Required Group ID from MiniMax account
 *   <li>{@code MINIMAX_BASE_URL} - Optional base URL override (defaults to international endpoint)
 * </ul>
 */
public final class MiniMaxConfig {

    public static final String API_KEY_ENV = "MINIMAX_API_KEY";
    public static final String GROUP_ID_ENV = "MINIMAX_GROUP_ID";
    public static final String BASE_URL_ENV = "MINIMAX_BASE_URL";
    public static final String DEFAULT_BASE_URL = "https://api.minimaxi.chat";
    public static final String DEFAULT_MODEL = "speech-2.6-hd";

    private MiniMaxConfig() {}

    public static String getApiKey() {
        String key = System.getenv(API_KEY_ENV);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    API_KEY_ENV + " environment variable is required for MiniMax TTS provider");
        }
        return key;
    }

    public static String getGroupId() {
        String groupId = System.getenv(GROUP_ID_ENV);
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalStateException(
                    GROUP_ID_ENV + " environment variable is required for MiniMax TTS provider");
        }
        return groupId;
    }

    public static String getBaseUrl() {
        String url = System.getenv(BASE_URL_ENV);
        return (url != null && !url.isBlank()) ? url : DEFAULT_BASE_URL;
    }

    public static String getModel() {
        return DEFAULT_MODEL;
    }
}
