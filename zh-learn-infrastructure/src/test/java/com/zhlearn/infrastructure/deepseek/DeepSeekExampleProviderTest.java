package com.zhlearn.infrastructure.deepseek;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.Optional;

class DeepSeekExampleProviderTest {
    
    private DeepSeekExampleProvider provider;
    
    @BeforeEach
    void setUp() {
        provider = new DeepSeekExampleProvider("test-api-key");
    }
    
    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("deepseek-chat");
    }
    
    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        DeepSeekExampleProvider customProvider = new DeepSeekExampleProvider(
            "custom-key", 
            "https://custom-url.com", 
            "deepseek-coder"
        );
        
        assertThat(customProvider.getName()).isEqualTo("deepseek-coder");
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
          .hasMessageContaining("Failed to get examples from DeepSeek API");
    }
}