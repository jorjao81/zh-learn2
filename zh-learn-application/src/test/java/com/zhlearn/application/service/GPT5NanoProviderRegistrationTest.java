package com.zhlearn.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.assertThat;

class GPT5NanoProviderRegistrationTest {
    
    private ProviderRegistry registry;
    private String originalApiKey;
    
    @BeforeEach
    void setUp() {
        originalApiKey = System.getenv("OPENAI_API_KEY");
        System.setProperty("OPENAI_API_KEY", "test-key-for-registration");
        registry = new ProviderRegistry();
    }
    
    @AfterEach 
    void tearDown() {
        System.clearProperty("OPENAI_API_KEY");
    }
    
    @Test
    void shouldRegisterGPT5NanoProviderWhenApiKeyIsAvailable() {
        var explanationProviders = registry.getAvailableExplanationProviders();
        assertThat(explanationProviders).contains("gpt-5-nano");
    }
    
    @Test
    void shouldNotRegisterGPT5NanoProviderWhenNoApiKey() {
        System.clearProperty("OPENAI_API_KEY");
        
        var explanationProviders = registry.getAvailableExplanationProviders();
        assertThat(explanationProviders).contains("gpt-5-nano");
    }
}