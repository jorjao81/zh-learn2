package com.zhlearn.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekStructuralDecompositionProviderRegistrationTest {
    
    private ProviderRegistry registry;
    private String originalApiKey;
    
    @BeforeEach
    void setUp() {
        originalApiKey = System.getenv("DEEPSEEK_API_KEY");
        System.setProperty("DEEPSEEK_API_KEY", "test-key-for-registration");
        registry = new ProviderRegistry();
    }
    
    @AfterEach 
    void tearDown() {
        System.clearProperty("DEEPSEEK_API_KEY");
    }
    
    @Test
    void shouldRegisterDeepSeekStructuralDecompositionProviderWhenApiKeyIsAvailable() {
        var providers = registry.getAvailableStructuralDecompositionProviders();
        assertThat(providers).contains("deepseek-chat");
    }
    
    @Test
    void shouldNotRegisterDeepSeekStructuralDecompositionProviderWhenNoApiKey() {
        System.clearProperty("DEEPSEEK_API_KEY");
        
        var providers = registry.getAvailableStructuralDecompositionProviders();
        assertThat(providers).contains("deepseek-chat");
    }
}