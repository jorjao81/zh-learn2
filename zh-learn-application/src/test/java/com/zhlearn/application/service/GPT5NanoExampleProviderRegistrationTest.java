package com.zhlearn.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.assertThat;

class GPT5NanoExampleProviderRegistrationTest {
    
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
    void shouldRegisterGPT5NanoExampleProviderWhenApiKeyIsAvailable() {
        var providers = registry.getAvailableExampleProviders();
        assertThat(providers).contains("gpt-5-nano");
    }
    
    @Test
    void shouldNotRegisterGPT5NanoExampleProviderWhenNoApiKey() {
        System.clearProperty("OPENAI_API_KEY");
        
        var providers = registry.getAvailableExampleProviders();
        assertThat(providers).contains("gpt-5-nano");
    }
}