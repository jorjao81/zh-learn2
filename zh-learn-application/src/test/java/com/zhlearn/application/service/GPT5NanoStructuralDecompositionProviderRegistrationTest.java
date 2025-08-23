package com.zhlearn.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.assertThat;

class GPT5NanoStructuralDecompositionProviderRegistrationTest {
    
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
    void shouldRegisterGPT5NanoStructuralDecompositionProviderWhenApiKeyIsAvailable() {
        var providers = registry.getAvailableStructuralDecompositionProviders();
        assertThat(providers).contains("gpt-5-nano");
    }
    
    @Test
    void shouldNotRegisterGPT5NanoStructuralDecompositionProviderWhenNoApiKey() {
        System.clearProperty("OPENAI_API_KEY");
        
        var providers = registry.getAvailableStructuralDecompositionProviders();
        assertThat(providers).contains("gpt-5-nano");
    }
}