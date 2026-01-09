package com.zhlearn.infrastructure.forvo;

/**
 * Configuration for Forvo API.
 *
 * <p>Environment variables:
 *
 * <ul>
 *   <li>{@code FORVO_API_KEY} - Required API key from Forvo platform
 *   <li>{@code FORVO_BASE_URL} - Optional base URL override (defaults to production endpoint)
 * </ul>
 */
public final class ForvoConfig {

    public static final String API_KEY_ENV = "FORVO_API_KEY";
    public static final String API_KEY_PROP = "forvo.api.key";
    public static final String BASE_URL_ENV = "FORVO_BASE_URL";
    public static final String DEFAULT_BASE_URL = "https://apifree.forvo.com";

    private ForvoConfig() {}

    public static String getApiKey() {
        String key = System.getenv(API_KEY_ENV);
        if (key == null || key.isBlank()) {
            key = System.getProperty(API_KEY_PROP);
        }
        return key;
    }

    public static String getBaseUrl() {
        String url = System.getenv(BASE_URL_ENV);
        return (url != null && !url.isBlank()) ? url : DEFAULT_BASE_URL;
    }

    /**
     * Builds the word-pronunciations endpoint URL.
     *
     * @param apiKey the API key
     * @param encodedWord URL-encoded word
     * @param perPage number of results per page
     * @return the complete API URL
     */
    public static String buildPronunciationUrl(String apiKey, String encodedWord, int perPage) {
        return getBaseUrl()
                + "/key/"
                + apiKey
                + "/format/json/action/word-pronunciations/word/"
                + encodedWord
                + "/language/zh/porder/rate-desc/perpage/"
                + perPage;
    }
}
