package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.DummyExampleProvider;
import com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
import com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AIProviderFactoryTest {

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("DEEPSEEK_API_KEY");
        System.clearProperty("DEEPSEEK_BASE_URL");
    }

    @Test
    void shouldReturnDummyExampleProvider() {
        ExampleProvider provider = AIProviderFactory.createExampleProvider("dummy");

        assertThat(provider).isInstanceOf(DummyExampleProvider.class);
    }

    @Test
    void shouldCreateConfigurableExampleProviderWhenApiKeyPresent() {
        System.setProperty("DEEPSEEK_API_KEY", "test-key");

        ExampleProvider provider = AIProviderFactory.createExampleProvider("deepseek-chat");

        assertThat(provider).isInstanceOf(ConfigurableExampleProvider.class);
        assertThat(provider.getName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldReturnDummyExplanationProvider() {
        ExplanationProvider provider = AIProviderFactory.createExplanationProvider("dummy");

        assertThat(provider).isInstanceOf(DummyExplanationProvider.class);
    }

    @Test
    void shouldCreateConfigurableExplanationProviderWhenApiKeyPresent() {
        System.setProperty("DEEPSEEK_API_KEY", "test-key");

        ExplanationProvider provider = AIProviderFactory.createExplanationProvider("deepseek-chat");

        assertThat(provider).isInstanceOf(ConfigurableExplanationProvider.class);
        assertThat(provider.getName()).isEqualTo("deepseek-chat");
    }

    @Test
    void shouldReturnDummyDecompositionProvider() {
        StructuralDecompositionProvider provider = AIProviderFactory.createDecompositionProvider("dummy");

        assertThat(provider).isInstanceOf(DummyStructuralDecompositionProvider.class);
    }

    @Test
    void shouldCreateConfigurableDecompositionProviderWhenApiKeyPresent() {
        System.setProperty("DEEPSEEK_API_KEY", "test-key");

        StructuralDecompositionProvider provider = AIProviderFactory.createDecompositionProvider("deepseek-chat");

        assertThat(provider).isInstanceOf(ConfigurableStructuralDecompositionProvider.class);
        assertThat(provider.getName()).isEqualTo("deepseek-chat");
    }
}

