package com.zhlearn.infrastructure.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CacheKeyGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CacheKeyGenerator() {}

    public String generateKey(
            String prompt,
            String baseUrl,
            String modelName,
            Double temperature,
            Integer maxTokens) {
        try {
            ObjectNode keyData = objectMapper.createObjectNode();
            keyData.put("prompt", prompt);
            keyData.put("baseUrl", baseUrl);
            keyData.put("modelName", modelName);

            if (temperature != null) {
                keyData.put("temperature", temperature);
            }

            if (maxTokens != null) {
                keyData.put("maxTokens", maxTokens);
            }

            String json = objectMapper.writeValueAsString(keyData);
            return hashToSha256(json);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate cache key", e);
        }
    }

    private String hashToSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
