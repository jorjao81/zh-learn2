package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatGLM45StructuralDecompositionProviderTest {

    private ChatGLM45StructuralDecompositionProvider provider;

    @BeforeEach
    void setUp() { provider = new ChatGLM45StructuralDecompositionProvider("test-api-key"); }

    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("glm-4.5");
    }

    @Test
    void shouldHandleBasicWordInput() {
        Hanzi word = new Hanzi("爱");

        assertThatThrownBy(() -> {
            StructuralDecomposition sd = provider.getStructuralDecomposition(word);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to get structural decomposition from ChatGLM (z.ai) API");
    }
}

