package com.zhlearn.infrastructure.qwen;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhlearn.domain.exception.UnrecoverableProviderException;
import com.zhlearn.infrastructure.common.CheckedExceptionWrapper;

import io.helidon.faulttolerance.Retry;

class QwenTtsClient {
    private static final Logger log = LoggerFactory.getLogger(QwenTtsClient.class);

    private static final URI ENDPOINT =
            URI.create(
                    "https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration INITIAL_BACKOFF = Duration.ofSeconds(5);
    private static final double BACKOFF_FACTOR = 3.0D;
    private static final Duration OVERALL_TIMEOUT = Duration.ofMinutes(15);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private final Retry retry;

    QwenTtsClient(HttpClient httpClient, String apiKey, String model) {
        this(httpClient, apiKey, model, new ObjectMapper());
    }

    QwenTtsClient(HttpClient httpClient, String apiKey, String model, ObjectMapper mapper) {
        this(httpClient, apiKey, model, mapper, defaultRetry());
    }

    QwenTtsClient(
            HttpClient httpClient, String apiKey, String model, ObjectMapper mapper, Retry retry) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key missing for DashScope request");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model name missing for DashScope request");
        }
        this.httpClient = httpClient;
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.model = model;
        this.retry = retry;
    }

    public QwenTtsResult synthesize(String voice, String text)
            throws IOException, InterruptedException, UnrecoverableProviderException {
        try {
            return retry.invoke(
                    () -> {
                        try {
                            return synthesizeOnce(voice, text);
                        } catch (IOException e) {
                            throw CheckedExceptionWrapper.wrap(e);
                        } catch (InterruptedException e) {
                            throw CheckedExceptionWrapper.wrap(e);
                        } catch (UnrecoverableProviderException e) {
                            // UnrecoverableProviderException is checked, wrap it to propagate
                            // through retry
                            throw CheckedExceptionWrapper.wrap(e);
                        }
                    });
        } catch (RateLimitException rateLimit) {
            log.warn(
                    "[QwenTTS] Rate limit exhausted after {} attempts for voice '{}'",
                    MAX_ATTEMPTS,
                    voice);
            throw new IOException("DashScope rate limit exhausted after retries", rateLimit);
        } catch (CheckedExceptionWrapper wrapper) {
            throw wrapper.unwrap();
        }
    }

    private QwenTtsResult synthesizeOnce(String voice, String text)
            throws IOException, InterruptedException, UnrecoverableProviderException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        Map<String, Object> input = new HashMap<>();
        input.put("text", text);
        input.put("voice", voice);
        payload.put("input", input);

        String body = mapper.writeValueAsString(payload);

        HttpRequest request =
                HttpRequest.newBuilder(ENDPOINT)
                        .timeout(TIMEOUT)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build();

        log.debug(
                "[QwenTTS] Making TTS request for voice '{}', text length: {}",
                voice,
                text.length());
        HttpResponse<String> response =
                httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorMessage =
                    "DashScope TTS failed: HTTP " + response.statusCode() + " - " + response.body();
            if (response.statusCode() == 429) {
                log.warn(
                        "[QwenTTS] Rate limit hit (HTTP 429) for voice '{}': {}",
                        voice,
                        response.body());
                throw new RateLimitException(errorMessage);
            } else if (response.statusCode() == 400
                    && response.body().contains("DataInspectionFailed")) {
                log.warn(
                        "[QwenTTS] Content moderation failed for voice '{}': {}",
                        voice,
                        response.body());
                throw new ContentModerationException(
                        "Content rejected by moderation policy: " + response.body());
            } else {
                log.error(
                        "[QwenTTS] TTS request failed with HTTP {} for voice '{}': {}",
                        response.statusCode(),
                        voice,
                        response.body());
                throw new IOException(errorMessage);
            }
        }

        log.debug(
                "[QwenTTS] TTS request successful for voice '{}', status: {}",
                voice,
                response.statusCode());

        JsonNode root = mapper.readTree(response.body());
        JsonNode urlNode = root.at("/output/audio/url");
        if (urlNode.isMissingNode() || urlNode.asText().isBlank()) {
            throw new IOException("DashScope response missing audio URL: " + response.body());
        }
        String requestId = root.path("request_id").asText(null);
        return new QwenTtsResult(URI.create(urlNode.asText()), requestId);
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
