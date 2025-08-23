package com.zhlearn.infrastructure.gpt5nano;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GPT5NanoExplanationProviderTest {
    
    private GPT5NanoExplanationProvider provider;
    
    @BeforeEach
    void setUp() {
        provider = new GPT5NanoExplanationProvider("test-api-key");
    }
    
    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("gpt-5-nano");
    }
    
    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        GPT5NanoExplanationProvider customProvider = new GPT5NanoExplanationProvider(
            "custom-key", 
            "https://custom-url.com", 
            "gpt-5-nano"
        );
        
        assertThat(customProvider.getName()).isEqualTo("gpt-5-nano");
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
        
        assertThatThrownBy(() -> {
            Explanation explanation = provider.getExplanation(word);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to get explanation from GPT-5 Nano API");
    }
}