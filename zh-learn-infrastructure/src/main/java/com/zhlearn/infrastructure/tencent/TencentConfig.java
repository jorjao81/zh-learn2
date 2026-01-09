package com.zhlearn.infrastructure.tencent;

/**
 * Configuration for Tencent Cloud TTS API.
 *
 * <p>Environment variables:
 *
 * <ul>
 *   <li>{@code TENCENT_SECRET_ID} - Required secret ID from Tencent Cloud
 *   <li>{@code TENCENT_API_KEY} - Required API key (secret key) from Tencent Cloud
 *   <li>{@code TENCENT_REGION} - Optional region (defaults to ap-singapore)
 *   <li>{@code TENCENT_BASE_URL} - Optional base URL override for testing (defaults to
 *       tts.tencentcloudapi.com)
 * </ul>
 */
public final class TencentConfig {

    public static final String SECRET_ID_ENV = "TENCENT_SECRET_ID";
    public static final String SECRET_KEY_ENV = "TENCENT_API_KEY";
    public static final String REGION_ENV = "TENCENT_REGION";
    public static final String BASE_URL_ENV = "TENCENT_BASE_URL";
    public static final String DEFAULT_REGION = "ap-singapore";
    public static final String DEFAULT_ENDPOINT = "tts.tencentcloudapi.com";

    private TencentConfig() {}

    public static String getSecretId() {
        String secretId = System.getenv(SECRET_ID_ENV);
        if (secretId == null || secretId.isBlank()) {
            throw new IllegalStateException(
                    SECRET_ID_ENV + " environment variable is required for Tencent TTS provider");
        }
        return secretId;
    }

    public static String getSecretKey() {
        String secretKey = System.getenv(SECRET_KEY_ENV);
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    SECRET_KEY_ENV + " environment variable is required for Tencent TTS provider");
        }
        return secretKey;
    }

    public static String getRegion() {
        String region = System.getenv(REGION_ENV);
        return (region != null && !region.isBlank()) ? region : DEFAULT_REGION;
    }

    /**
     * Returns the endpoint for Tencent TTS API. If TENCENT_BASE_URL is set, extracts the host from
     * it. Otherwise returns the default endpoint.
     */
    public static String getEndpoint() {
        String baseUrl = System.getenv(BASE_URL_ENV);
        if (baseUrl != null && !baseUrl.isBlank()) {
            // Extract host from URL like "http://localhost:8080" -> "localhost:8080"
            return baseUrl.replaceFirst("^https?://", "");
        }
        return DEFAULT_ENDPOINT;
    }

    /** Returns the protocol (http or https) based on TENCENT_BASE_URL. */
    public static String getProtocol() {
        String baseUrl = System.getenv(BASE_URL_ENV);
        if (baseUrl != null && baseUrl.startsWith("http://")) {
            return "http://";
        }
        return "https://";
    }

    /** Returns true if a custom base URL is configured (for testing). */
    public static boolean hasCustomEndpoint() {
        String baseUrl = System.getenv(BASE_URL_ENV);
        return baseUrl != null && !baseUrl.isBlank();
    }
}
