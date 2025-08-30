package com.zhlearn.infrastructure.gpt5nano;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Optional;

class GPT5NanoExampleProviderTest {
    
    private GPT5NanoExampleProvider provider;
    
    @BeforeEach
    void setUp() {
        provider = new GPT5NanoExampleProvider("test-api-key");
    }
    
    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("gpt-5-nano");
    }
    
    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        GPT5NanoExampleProvider customProvider = new GPT5NanoExampleProvider(
            "custom-key", 
            "https://custom-url.com", 
            "gpt-5-nano"
        );
        
        assertThat(customProvider.getName()).isEqualTo("gpt-5-nano");
    }
    
    @Test
    void shouldThrowExceptionForInvalidWord() {
        assertThatThrownBy(() -> {
            provider.getExamples(null, Optional.empty());
        }).isInstanceOf(Exception.class);
    }
    
    @Test
    void shouldHandleBasicWordInput() {
        Hanzi word = new Hanzi("你好");
        
        assertThatThrownBy(() -> {
            Example examples = provider.getExamples(word, Optional.empty());
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to get examples from GPT-5 Nano API");
    }
}