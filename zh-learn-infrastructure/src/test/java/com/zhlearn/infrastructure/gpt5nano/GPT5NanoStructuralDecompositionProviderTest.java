package com.zhlearn.infrastructure.gpt5nano;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GPT5NanoStructuralDecompositionProviderTest {
    
    private GPT5NanoStructuralDecompositionProvider provider;
    
    @BeforeEach
    void setUp() {
        provider = new GPT5NanoStructuralDecompositionProvider("test-api-key");
    }
    
    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("gpt-5-nano");
    }
    
    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        GPT5NanoStructuralDecompositionProvider customProvider = new GPT5NanoStructuralDecompositionProvider(
            "custom-key", 
            "https://custom-url.com", 
            "gpt-5-nano"
        );
        
        assertThat(customProvider.getName()).isEqualTo("gpt-5-nano");
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
          .hasMessageContaining("Failed to get structural decomposition from GPT-5 Nano API");
    }
}