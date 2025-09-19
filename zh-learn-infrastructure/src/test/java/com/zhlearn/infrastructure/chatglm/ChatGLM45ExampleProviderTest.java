package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatGLM45ExampleProviderTest {

    private ChatGLM45ExampleProvider provider;

    @BeforeEach
    void setUp() { provider = new ChatGLM45ExampleProvider("test-api-key"); }

    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("glm-4.5");
    }

    @Test
    void shouldHandleBasicWordInput() {
        Hanzi word = new Hanzi("你好");

        assertThatThrownBy(() -> {
            Example examples = provider.getExamples(word, Optional.empty());
        }).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Failed to get examples from ChatGLM (z.ai) API");
    }
}

