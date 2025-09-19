package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatGLMExplanationProviderTest {

    private ChatGLMExplanationProvider provider;

    @BeforeEach
    void setUp() { provider = new ChatGLMExplanationProvider("test-api-key"); }

    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("glm-4-flash");
    }

    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        ChatGLMExplanationProvider customProvider = new ChatGLMExplanationProvider(
            "custom-key",
            "https://custom-url.com",
            "glm-4"
        );

        assertThat(customProvider.getName()).isEqualTo("glm-4");
    }

    @Test
    void shouldThrowExceptionForInvalidWord() {
        assertThatThrownBy(() -> provider.getExplanation(null))
            .isInstanceOf(Exception.class);
    }

    @Test
    void shouldHandleBasicWordInput() {
        Hanzi word = new Hanzi("学习");

        assertThatThrownBy(() -> {
            Explanation exp = provider.getExplanation(word);
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to get explanation from ChatGLM (z.ai) API");
    }
}

