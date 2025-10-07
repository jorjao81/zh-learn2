package com.zhlearn.infrastructure.common;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.chat.ChatModel;

/**
 * Minimal OpenAI-compatible ChatModel for z.ai/GLM endpoints. Tries multiple known base URL
 * patterns to avoid 404 path mismatches.
 */
public class ZaiOpenAiChatModel implements ChatModel {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http;
    private final String apiKey;
    private final String baseUrl; // preferred base URL
    private final String modelName;
    private final Double temperature;
    private final Integer maxTokens;

    public ZaiOpenAiChatModel(
            String apiKey,
            String baseUrl,
            String modelName,
            Double temperature,
            Integer maxTokens) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    @Override
    public String chat(String prompt) {
        RuntimeException lastError = null;
        for (String base : candidateBases(baseUrl)) {
            try {
                String endpoint =
                        base.endsWith("/") ? base + "chat/completions" : base + "/chat/completions";
                String body = buildRequestBody(prompt);
                HttpRequest req =
                        HttpRequest.newBuilder()
                                .uri(URI.create(endpoint))
                                .timeout(Duration.ofSeconds(120))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + apiKey)
                                // Some gateways use custom header; include both to maximize
                                // compatibility
                                .header("X-API-Key", apiKey)
                                .POST(
                                        HttpRequest.BodyPublishers.ofString(
                                                body, StandardCharsets.UTF_8))
                                .build();
                HttpResponse<String> resp =
                        http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    return extractContent(resp.body());
                } else if (resp.statusCode() == 404) {
                    lastError = new RuntimeException("404 at " + endpoint);
                    continue; // try next candidate base
                } else {
                    throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
                }
            } catch (Exception e) {
                lastError = new RuntimeException(e);
            }
        }
        if (lastError != null) throw lastError;
        throw new RuntimeException("Failed to call z.ai chat completions");
    }

    private List<String> candidateBases(String preferred) {
        // Order: preferred -> common variants
        return List.of(
                nonNullOrDefault(preferred, "https://api.z.ai/openai/v1"),
                "https://api.z.ai/openai/v1",
                "https://api.z.ai/v1",
                "https://open.bigmodel.cn/api/paas/v4");
    }

    private String nonNullOrDefault(String value, String def) {
        return (value == null || value.isBlank()) ? def : value;
    }

    private String buildRequestBody(String prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"model\":").append('"').append(escape(modelName)).append('"');
        if (temperature != null) sb.append(',').append("\"temperature\":").append(temperature);
        if (maxTokens != null) sb.append(',').append("\"max_tokens\":").append(maxTokens);
        // Basic OpenAI-compatible messages payload
        sb.append(',').append("\"messages\":[");
        sb.append("{\"role\":\"user\",\"content\":")
                .append('"')
                .append(escape(prompt))
                .append('"')
                .append('}');
        sb.append(']');
        sb.append('}');
        return sb.toString();
    }

    private String extractContent(String json) throws Exception {
        JsonNode root = MAPPER.readTree(json);
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode msg = choices.get(0).path("message");
            if (!msg.isMissingNode()) {
                JsonNode content = msg.path("content");
                if (!content.isMissingNode()) return content.asText();
            }
            // Some variants return choices[0].text
            JsonNode text = choices.get(0).path("text");
            if (!text.isMissingNode()) return text.asText();
        }
        // Fallback to full JSON
        return json;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
