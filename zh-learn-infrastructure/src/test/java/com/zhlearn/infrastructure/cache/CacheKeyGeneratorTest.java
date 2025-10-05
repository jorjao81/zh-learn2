package com.zhlearn.infrastructure.cache;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CacheKeyGeneratorTest {
    private final CacheKeyGenerator generator = new CacheKeyGenerator();

    @Test
    void shouldGenerateConsistentKeysForSameParameters() {
        String prompt = "Test prompt";
        String baseUrl = "https://api.example.com";
        String modelName = "test-model";
        Double temperature = 0.3;
        Integer maxTokens = 1000;

        String key1 = generator.generateKey(prompt, baseUrl, modelName, temperature, maxTokens);
        String key2 = generator.generateKey(prompt, baseUrl, modelName, temperature, maxTokens);

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void shouldGenerateDifferentKeysForDifferentPrompts() {
        String baseUrl = "https://api.example.com";
        String modelName = "test-model";
        Double temperature = 0.3;
        Integer maxTokens = 1000;

        String key1 = generator.generateKey("Prompt 1", baseUrl, modelName, temperature, maxTokens);
        String key2 = generator.generateKey("Prompt 2", baseUrl, modelName, temperature, maxTokens);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldGenerateDifferentKeysForDifferentTemperatures() {
        String prompt = "Test prompt";
        String baseUrl = "https://api.example.com";
        String modelName = "test-model";
        Integer maxTokens = 1000;

        String key1 = generator.generateKey(prompt, baseUrl, modelName, 0.3, maxTokens);
        String key2 = generator.generateKey(prompt, baseUrl, modelName, 0.7, maxTokens);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldHandleNullTemperatureAndMaxTokens() {
        String prompt = "Test prompt";
        String baseUrl = "https://api.example.com";
        String modelName = "test-model";

        String key1 = generator.generateKey(prompt, baseUrl, modelName, null, null);
        String key2 = generator.generateKey(prompt, baseUrl, modelName, null, null);

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void shouldGenerateValidSHA256Hash() {
        String key = generator.generateKey("test", "url", "model", 0.5, 100);
        
        assertThat(key).isNotNull();
        assertThat(key).hasSize(64); // SHA-256 produces 64 character hex string
        assertThat(key).matches("^[0-9a-f]+$"); // Only hex characters
    }
}