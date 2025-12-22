package com.zhlearn.infrastructure.minimax;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhlearn.domain.exception.UnrecoverableProviderException;
import com.zhlearn.infrastructure.common.CheckedExceptionWrapper;
import com.zhlearn.infrastructure.ratelimit.ProviderRateLimiter;

import io.helidon.faulttolerance.Retry;

/**
 * HTTP client for MiniMax TTS API (speech-2.6-hd model). Uses synchronous mode with hex-encoded
 * audio response for simplicity.
 */
class MiniMaxTtsClient {
    private static final Logger log = LoggerFactory.getLogger(MiniMaxTtsClient.class);

    private static final String ENDPOINT_PATH = "/v1/t2a_v2";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration INITIAL_BACKOFF = Duration.ofSeconds(5);
    private static final double BACKOFF_FACTOR = 3.0D;
    private static final Duration OVERALL_TIMEOUT = Duration.ofMinutes(15);
    private static final Duration RATE_LIMIT_ACQUIRE_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String groupId;
    private final String baseUrl;
    private final String model;
    private final Retry retry;
    private final ProviderRateLimiter rateLimiter;

    MiniMaxTtsClient(
            HttpClient httpClient, String apiKey, String groupId, String baseUrl, String model) {
        this(httpClient, apiKey, groupId, baseUrl, model, new ObjectMapper(), defaultRetry(), null);
    }

    MiniMaxTtsClient(
            HttpClient httpClient,
            String apiKey,
            String groupId,
            String baseUrl,
            String model,
            ObjectMapper mapper,
            Retry retry,
            ProviderRateLimiter rateLimiter) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key missing for MiniMax TTS request");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("Group ID missing for MiniMax TTS request");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL missing for MiniMax TTS request");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model name missing for MiniMax TTS request");
        }
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.groupId = groupId;
        this.baseUrl = baseUrl;
        this.model = model;
        this.retry = retry != null ? retry : defaultRetry();
        this.rateLimiter = rateLimiter;
    }

    public MiniMaxTtsResult synthesize(String voiceId, String text)
            throws IOException, InterruptedException, UnrecoverableProviderException {
        // Acquire rate limit permit before making request (if rate limiter configured)
        if (rateLimiter != null) {
            boolean acquired = rateLimiter.acquire(RATE_LIMIT_ACQUIRE_TIMEOUT);
            if (!acquired) {
                throw new IOException(
                        "Rate limit timeout - provider overwhelmed after waiting "
                                + RATE_LIMIT_ACQUIRE_TIMEOUT.toSeconds()
                                + "s");
            }
        }

        try {
            MiniMaxTtsResult result =
                    retry.invoke(
                            () -> {
                                try {
                                    return synthesizeOnce(voiceId, text);
                                } catch (IOException e) {
                                    throw CheckedExceptionWrapper.wrap(e);
                                } catch (InterruptedException e) {
                                    throw CheckedExceptionWrapper.wrap(e);
                                } catch (UnrecoverableProviderException e) {
                                    throw CheckedExceptionWrapper.wrap(e);
                                } catch (RateLimitException e) {
                                    // Notify rate limiter so ALL pending requests slow down
                                    if (rateLimiter != null) {
                                        rateLimiter.notifyRateLimited(null);
                                    }
                                    throw e; // Let Retry handle the retry logic
                                }
                            });

            // Notify success for rate recovery
            if (rateLimiter != null) {
                rateLimiter.notifySuccess();
            }
            return result;

        } catch (RateLimitException rateLimit) {
            log.warn(
                    "[MiniMaxTTS] Rate limit exhausted after {} attempts for voice '{}'",
                    MAX_ATTEMPTS,
                    voiceId);
            throw new IOException("MiniMax rate limit exhausted after retries", rateLimit);
        } catch (CheckedExceptionWrapper wrapper) {
            wrapper.unwrap();
            throw new AssertionError("unreachable");
        }
    }

    private MiniMaxTtsResult synthesizeOnce(String voiceId, String text)
            throws IOException, InterruptedException, UnrecoverableProviderException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);
        payload.put("text", text);
        payload.put("stream", false);
        payload.put("output_format", "hex");
        payload.put("language_boost", "Chinese");

        // Voice settings
        ObjectNode voiceSetting = payload.putObject("voice_setting");
        voiceSetting.put("voice_id", voiceId);
        voiceSetting.put("speed", 1.0);
        voiceSetting.put("vol", 1.0);
        voiceSetting.put("pitch", 0);
        voiceSetting.put("emotion", "neutral");

        // Audio settings
        ObjectNode audioSetting = payload.putObject("audio_setting");
        audioSetting.put("sample_rate", 32000);
        audioSetting.put("bitrate", 128000);
        audioSetting.put("format", "mp3");
        audioSetting.put("channel", 1);

        String body = mapper.writeValueAsString(payload);

        URI endpoint = URI.create(baseUrl + ENDPOINT_PATH + "?GroupId=" + groupId);
        HttpRequest request =
                HttpRequest.newBuilder(endpoint)
                        .timeout(TIMEOUT)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build();

        log.debug(
                "[MiniMaxTTS] Making TTS request for voice '{}', text length: {}",
                voiceId,
                text.length());
        HttpResponse<String> response =
                httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorMessage =
                    "MiniMax TTS failed: HTTP " + response.statusCode() + " - " + response.body();
            if (response.statusCode() == 429) {
                log.warn(
                        "[MiniMaxTTS] Rate limit hit (HTTP 429) for voice '{}': {}",
                        voiceId,
                        response.body());
                throw new RateLimitException(errorMessage);
            } else if (response.statusCode() == 400) {
                log.warn("[MiniMaxTTS] Bad request for voice '{}': {}", voiceId, response.body());
                throw new MiniMaxContentException("Request rejected: " + response.body());
            } else {
                log.error(
                        "[MiniMaxTTS] TTS request failed with HTTP {} for voice '{}': {}",
                        response.statusCode(),
                        voiceId,
                        response.body());
                throw new IOException(errorMessage);
            }
        }

        log.debug(
                "[MiniMaxTTS] TTS request successful for voice '{}', status: {}",
                voiceId,
                response.statusCode());

        JsonNode root = mapper.readTree(response.body());

        // Check for API-level errors
        JsonNode baseResp = root.path("base_resp");
        int statusCode = baseResp.path("status_code").asInt(0);
        if (statusCode != 0) {
            String statusMsg = baseResp.path("status_msg").asText("Unknown error");
            log.error(
                    "[MiniMaxTTS] API error for voice '{}': {} - {}",
                    voiceId,
                    statusCode,
                    statusMsg);
            throw new IOException("MiniMax API error: " + statusCode + " - " + statusMsg);
        }

        // Extract hex-encoded audio
        JsonNode dataNode = root.path("data");
        JsonNode audioNode = dataNode.path("audio");
        if (audioNode.isMissingNode() || audioNode.asText().isBlank()) {
            throw new IOException("MiniMax response missing audio data: " + response.body());
        }

        String hexAudio = audioNode.asText();
        byte[] audioData = HexFormat.of().parseHex(hexAudio);

        String traceId = root.path("trace_id").asText(null);
        return new MiniMaxTtsResult(audioData, traceId);
    }

    private static Retry defaultRetry() {
        return Retry.create(
                builder ->
                        builder.calls(MAX_ATTEMPTS)
                                .delay(INITIAL_BACKOFF)
                                .delayFactor(BACKOFF_FACTOR)
                                .overallTimeout(OVERALL_TIMEOUT)
                                .applyOn(Set.of(RateLimitException.class)));
    }

    static final class RateLimitException extends RuntimeException {
        RateLimitException(String message) {
            super(message);
        }
    }
}
