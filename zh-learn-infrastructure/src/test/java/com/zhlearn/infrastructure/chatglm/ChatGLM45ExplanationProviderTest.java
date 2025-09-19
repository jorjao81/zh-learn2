package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatGLM45ExplanationProviderTest {

    private ChatGLM45ExplanationProvider provider;

    @BeforeEach
    void setUp() { provider = new ChatGLM45ExplanationProvider("test-api-key"); }

    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("glm-4.5");
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

