package com.zhlearn.infrastructure.image;

/** Configuration utility for Google Custom Search API credentials and settings. */
public final class GoogleImageSearchConfig {
    private static final String API_KEY_ENV_VAR = "GOOGLE_SEARCH_API_KEY";
    private static final String SEARCH_ENGINE_ID_ENV_VAR = "GOOGLE_SEARCH_ENGINE_ID";
    private static final String DEFAULT_ENDPOINT = "https://www.googleapis.com/customsearch/v1";

    private GoogleImageSearchConfig() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the Google Custom Search API key from environment variables.
     *
     * @return the API key
     * @throws IllegalStateException if API key is not configured
     */
    public static String getApiKey() {
        String apiKey = System.getenv(API_KEY_ENV_VAR);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Google Search API key not configured. Set "
                            + API_KEY_ENV_VAR
                            + " environment variable. "
                            + "See IMAGE_FEATURE_IMPLEMENTATION_PLAN.md for setup instructions.");
        }
        return apiKey;
    }

    /**
     * Get the Google Custom Search Engine ID from environment variables.
     *
     * @return the search engine ID
     * @throws IllegalStateException if search engine ID is not configured
     */
    public static String getSearchEngineId() {
        String engineId = System.getenv(SEARCH_ENGINE_ID_ENV_VAR);
        if (engineId == null || engineId.isBlank()) {
            throw new IllegalStateException(
                    "Google Search engine ID not configured. Set "
                            + SEARCH_ENGINE_ID_ENV_VAR
                            + " environment variable. "
                            + "See IMAGE_FEATURE_IMPLEMENTATION_PLAN.md for setup instructions.");
        }
        return engineId;
    }

    /**
     * Get the Google Custom Search API endpoint URL.
     *
     * @return the endpoint URL
     */
    public static String getEndpoint() {
        return DEFAULT_ENDPOINT;
    }
}
