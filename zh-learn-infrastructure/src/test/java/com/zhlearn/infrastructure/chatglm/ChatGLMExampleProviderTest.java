package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatGLMExampleProviderTest {

    private ChatGLMExampleProvider provider;

    @BeforeEach
    void setUp() { provider = new ChatGLMExampleProvider("test-api-key"); }

    @Test
    void shouldReturnCorrectProviderName() {
        String name = provider.getName();
        assertThat(name).isEqualTo("glm-4-flash");
    }

    @Test
    void shouldCreateProviderWithCustomConfiguration() {
        ChatGLMExampleProvider customProvider = new ChatGLMExampleProvider(
            "custom-key",
            "https://custom-url.com",
            "glm-4"
        );

        assertThat(customProvider.getName()).isEqualTo("glm-4");
    }

    @Test
    void shouldThrowExceptionForInvalidWord() {
        assertThatThrownBy(() -> provider.getExamples(null, Optional.empty()))
            .isInstanceOf(Exception.class);
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

