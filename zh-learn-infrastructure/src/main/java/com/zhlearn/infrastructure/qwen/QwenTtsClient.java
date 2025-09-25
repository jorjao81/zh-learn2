package com.zhlearn.infrastructure.qwen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.faulttolerance.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

class QwenTtsClient {
    private static final Logger log = LoggerFactory.getLogger(QwenTtsClient.class);

    private static final URI ENDPOINT = URI.create(
        "https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private final Retry retry;

    QwenTtsClient(HttpClient httpClient, String apiKey, String model) {
        this(httpClient, apiKey, model, new ObjectMapper());
    }

    QwenTtsClient(HttpClient httpClient, String apiKey, String model, ObjectMapper mapper) {
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

        // Configure exponential backoff retry for rate limiting
        this.retry = Retry.builder()
            .retryPolicy(Retry.DelayingRetryPolicy.builder()
                .calls(5)                           // Total calls (1 initial + 4 retries)
                .delay(Duration.ofMillis(5000))     // Initial delay: 5 seconds
                .delayFactor(3.0)                   // Exponential factor: 3x
                .build())
            .overallTimeout(Duration.ofMinutes(15))
            .build();
    }

    public QwenTtsResult synthesize(String voice, String text) throws IOException, InterruptedException {
        return retry.invoke(() -> {
            try {
                return synthesizeInternal(voice, text);
            } catch (Exception e) {
                if (e instanceof IOException ioException) {
                    // Check if this is a rate limit error (HTTP 429)
                    if (ioException.getMessage().contains("HTTP 429")) {
                        log.warn("[QwenTTS] Rate limit exceeded for voice '{}', text length {}, retrying with exponential backoff",
                            voice, text.length());
                        throw new RateLimitException(ioException);
                    }
                }
                // Re-throw all other exceptions as-is (no retry)
                throw new RuntimeException(e);
            }
        });
    }

    private QwenTtsResult synthesizeInternal(String voice, String text) throws IOException, InterruptedException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        Map<String, Object> input = new HashMap<>();
        input.put("text", text);
        input.put("voice", voice);
        payload.put("input", input);

        String body = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder(ENDPOINT)
            .timeout(TIMEOUT)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();

        log.debug("[QwenTTS] Making TTS request for voice '{}', text length: {}", voice, text.length());
        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorMessage = "DashScope TTS failed: HTTP " + response.statusCode() + " - " + response.body();
            if (response.statusCode() == 429) {
                log.warn("[QwenTTS] Rate limit hit (HTTP 429) for voice '{}': {}", voice, response.body());
            } else {
                log.error("[QwenTTS] TTS request failed with HTTP {} for voice '{}': {}",
                    response.statusCode(), voice, response.body());
            }
            throw new IOException(errorMessage);
        }

        log.debug("[QwenTTS] TTS request successful for voice '{}', status: {}", voice, response.statusCode());

        JsonNode root = mapper.readTree(response.body());
        JsonNode urlNode = root.at("/output/audio/url");
        if (urlNode.isMissingNode() || urlNode.asText().isBlank()) {
            throw new IOException("DashScope response missing audio URL: " + response.body());
        }
        String requestId = root.path("request_id").asText(null);
        return new QwenTtsResult(URI.create(urlNode.asText()), requestId);
    }

    // Custom exception to signal retryable rate limit errors
    private static class RateLimitException extends RuntimeException {
        public RateLimitException(IOException cause) {
            super(cause);
        }
    }
}
