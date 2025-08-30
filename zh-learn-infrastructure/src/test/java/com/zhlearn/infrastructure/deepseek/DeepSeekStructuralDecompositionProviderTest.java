package com.zhlearn.infrastructure.deepseek;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepSeekStructuralDecompositionProviderTest {
    
    private DeepSeekStructuralDecompositionProvider provider;
    
    @BeforeEach
    void setUp() {
        provider = new DeepSeekStructuralDecompositionProvider("test-api-key");
    }
    
    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("deepseek-chat");
    }
    
    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        DeepSeekStructuralDecompositionProvider customProvider = new DeepSeekStructuralDecompositionProvider(
            "custom-key", 
            "https://custom-url.com", 
            "deepseek-coder"
        );
        
        assertThat(customProvider.getName()).isEqualTo("deepseek-coder");
    }
    
    @Test
    void shouldThrowExceptionForInvalidWord() {
        assertThatThrownBy(() -> {
            provider.getStructuralDecomposition(null);
        }).isInstanceOf(Exception.class);
    }
    
    @Test
    void shouldHandleBasicWordInput() {
        Hanzi word = new Hanzi("你好");
        
        assertThatThrownBy(() -> {
            StructuralDecomposition decomposition = provider.getStructuralDecomposition(word);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to get structural decomposition from DeepSeek API");
    }
}