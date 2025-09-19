package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatGLMStructuralDecompositionProviderTest {

    private ChatGLMStructuralDecompositionProvider provider;

    @BeforeEach
    void setUp() { provider = new ChatGLMStructuralDecompositionProvider("test-api-key"); }

    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("glm-4-flash");
    }

    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        ChatGLMStructuralDecompositionProvider customProvider = new ChatGLMStructuralDecompositionProvider(
            "custom-key",
            "https://custom-url.com",
            "glm-4"
        );

        assertThat(customProvider.getName()).isEqualTo("glm-4");
    }

    @Test
    void shouldThrowExceptionForInvalidWord() {
        assertThatThrownBy(() -> provider.getStructuralDecomposition(null))
            .isInstanceOf(Exception.class);
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

