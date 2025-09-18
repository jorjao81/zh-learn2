package com.zhlearn.infrastructure.qwen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class RestQwenTtsClient implements QwenTtsClient {
    private static final URI ENDPOINT = URI.create(
        "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    RestQwenTtsClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.mapper = new ObjectMapper();
    }

    @Override
    public QwenTtsResult synthesize(String apiKey, String model, String voice, String text)
        throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("API key missing for DashScope request");
        }
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

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("DashScope TTS failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode urlNode = root.at("/output/audio/url");
        if (urlNode.isMissingNode() || urlNode.asText().isBlank()) {
            throw new IOException("DashScope response missing audio URL: " + response.body());
        }
        String requestId = root.path("request_id").asText(null);
        return new QwenTtsResult(URI.create(urlNode.asText()), requestId);
    }
}
