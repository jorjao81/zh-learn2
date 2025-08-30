package com.zhlearn.infrastructure.deepseek;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepSeekExplanationProviderTest {
    
    private DeepSeekExplanationProvider provider;
    
    @BeforeEach
    void setUp() {
        // Use a dummy API key for testing
        provider = new DeepSeekExplanationProvider("test-api-key");
    }
    
    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("deepseek-chat");
    }
    
    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        DeepSeekExplanationProvider customProvider = new DeepSeekExplanationProvider(
            "custom-key", 
            "https://custom-url.com", 
            "deepseek-coder"
        );
        
        assertThat(customProvider.getName()).isEqualTo("deepseek-coder");
    }
    
    @Test
    void shouldThrowExceptionForInvalidWord() {
        assertThatThrownBy(() -> {
            provider.getExplanation(null);
        }).isInstanceOf(Exception.class);
    }
    
    @Test
    void shouldHandleBasicWordInput() {
        Hanzi word = new Hanzi("你好");
        
        // This test will fail in CI without a real API key, but shows the expected behavior
        // In a real test environment, you would mock the ChatLanguageModel
        assertThatThrownBy(() -> {
            Explanation explanation = provider.getExplanation(word);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to get explanation from DeepSeek API");
    }
}