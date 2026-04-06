package com.zhlearn.infrastructure.tencent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhlearn.infrastructure.ratelimit.ProviderRateLimiter;

/**
 * HTTP client for Tencent Cloud TTS API using direct HTTP calls with TC3-HMAC-SHA256 signing.
 * Bypasses the Tencent SDK to avoid Gson/JPMS incompatibilities with Java 25 modules.
 */
class TencentTtsClient {
    private static final Logger log = LoggerFactory.getLogger(TencentTtsClient.class);
    private static final Duration RATE_LIMIT_ACQUIRE_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final String SERVICE = "tts";
    private static final String ACTION = "TextToVoice";
    private static final String VERSION = "2019-08-23";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String secretId;
    private final String secretKey;
    private final String region;
    private final String host;
    private final String endpoint;
    private final ProviderRateLimiter rateLimiter;

    TencentTtsClient(
            HttpClient httpClient,
            String secretId,
            String secretKey,
            String region,
            String host,
            String endpoint,
            ProviderRateLimiter rateLimiter) {
        if (secretId == null || secretId.isBlank()) {
            throw new IllegalArgumentException("Secret ID missing for Tencent Cloud request");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("Secret key missing for Tencent Cloud request");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region missing for Tencent Cloud request");
        }
        this.httpClient = httpClient;
        this.mapper = new ObjectMapper();
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.region = region;
        this.host = host != null && !host.isBlank() ? host : TencentConfig.DEFAULT_ENDPOINT;
        this.endpoint =
                endpoint != null && !endpoint.isBlank() ? endpoint : ("https://" + this.host);
        this.rateLimiter = rateLimiter;
    }

    public TencentTtsResult synthesize(int voiceType, String text)
            throws IOException, InterruptedException {
        if (rateLimiter != null) {
            boolean acquired = rateLimiter.acquire(RATE_LIMIT_ACQUIRE_TIMEOUT);
            if (!acquired) {
                throw new TencentTtsClientException(
                        "Rate limit timeout - provider overwhelmed after waiting "
                                + RATE_LIMIT_ACQUIRE_TIMEOUT.toSeconds()
                                + "s",
                        null);
            }
        }

        ObjectNode payload = mapper.createObjectNode();
        payload.put("Text", text);
        payload.put("SessionId", UUID.randomUUID().toString());
        payload.put("VoiceType", voiceType);
        payload.put("Codec", "mp3");
        payload.put("SampleRate", 16000);

        String body = mapper.writeValueAsString(payload);
        long timestamp = Instant.now().getEpochSecond();
        String date =
                DateTimeFormatter.ISO_LOCAL_DATE.format(
                        Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC));

        String authorization = sign(body, timestamp, date);

        HttpRequest request =
                HttpRequest.newBuilder(URI.create(endpoint))
                        .timeout(TIMEOUT)
                        .header("Content-Type", "application/json")
                        .header("X-TC-Action", ACTION)
                        .header("X-TC-Version", VERSION)
                        .header("X-TC-Timestamp", String.valueOf(timestamp))
                        .header("X-TC-Region", region)
                        .header("Authorization", authorization)
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build();

        log.debug(
                "[TencentTTS] Making TTS request for voice {}, text length: {}",
                voiceType,
                text.length());
        HttpResponse<String> response =
                httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        JsonNode root = mapper.readTree(response.body());
        JsonNode responseNode = root.path("Response");
        JsonNode errorNode = responseNode.path("Error");

        if (!errorNode.isMissingNode()) {
            String errorCode = errorNode.path("Code").asText("Unknown");
            String errorMessage = errorNode.path("Message").asText("Unknown error");
            log.error(
                    "[TencentTTS] API error for voice {}: {} - {}",
                    voiceType,
                    errorCode,
                    errorMessage);

            if (errorCode.contains("RequestLimitExceeded")
                    || errorCode.contains("RateLimitExceeded")) {
                if (rateLimiter != null) {
                    log.warn("[TencentTTS] Rate limit hit: {}", errorMessage);
                    rateLimiter.notifyRateLimited(null);
                }
            }
            throw new TencentTtsClientException(
                    "Tencent TTS API error: " + errorCode + " - " + errorMessage);
        }

        String audioData = responseNode.path("Audio").asText(null);
        if (audioData == null || audioData.isBlank()) {
            throw new IOException("Tencent TTS response missing audio data");
        }

        String sessionId = responseNode.path("SessionId").asText(null);

        if (rateLimiter != null) {
            rateLimiter.notifySuccess();
        }

        log.debug("[TencentTTS] TTS request successful for voice {}", voiceType);
        return new TencentTtsResult(audioData, sessionId);
    }

    private String sign(String body, long timestamp, String date) {
        String credentialScope = date + "/" + SERVICE + "/tc3_request";

        // Step 1: CanonicalRequest
        String hashedPayload = sha256Hex(body);
        String canonicalRequest =
                "POST\n"
                        + "/\n"
                        + "\n"
                        + "content-type:application/json\n"
                        + "host:"
                        + host
                        + "\n"
                        + "\n"
                        + "content-type;host\n"
                        + hashedPayload;

        // Step 2: StringToSign
        String stringToSign =
                ALGORITHM
                        + "\n"
                        + timestamp
                        + "\n"
                        + credentialScope
                        + "\n"
                        + sha256Hex(canonicalRequest);

        // Step 3: Signing key
        byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, SERVICE);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");

        // Step 4: Signature
        String signature = HexFormat.of().formatHex(hmacSha256(secretSigning, stringToSign));

        // Step 5: Authorization header
        return ALGORITHM
                + " Credential="
                + secretId
                + "/"
                + credentialScope
                + ", SignedHeaders=content-type;host, Signature="
                + signature;
    }

    private static String sha256Hex(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-256 not available", e);
        }
    }

    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AssertionError("HMAC-SHA256 not available", e);
        }
    }
}
