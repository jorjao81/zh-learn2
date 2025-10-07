package com.zhlearn.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

class AIProviderFactoryTest {
    private final AIProviderFactory factory = new AIProviderFactory();

    @Test
    void shouldCreateDeepSeekProvidersWithSingleAndMultiConfigs() {
        String previous = System.getProperty("DEEPSEEK_API_KEY");
        try {
            System.setProperty("DEEPSEEK_API_KEY", "test-key");

            ExampleProvider exampleProvider = factory.createExampleProvider("deepseek-chat");
            assertThat(exampleProvider).isInstanceOf(ConfigurableExampleProvider.class);
            ConfigurableExampleProvider configurableExampleProvider =
                    (ConfigurableExampleProvider) exampleProvider;
            assertThat(configurableExampleProvider.singleCharConfig()).isPresent();
            assertThat(configurableExampleProvider.multiCharConfig()).isPresent();

            ExplanationProvider explanationProvider =
                    factory.createExplanationProvider("deepseek-chat");
            assertThat(explanationProvider).isInstanceOf(ConfigurableExplanationProvider.class);
            ConfigurableExplanationProvider configurableExplanationProvider =
                    (ConfigurableExplanationProvider) explanationProvider;
            assertThat(configurableExplanationProvider.singleCharConfig()).isPresent();
            assertThat(configurableExplanationProvider.multiCharConfig()).isPresent();

            StructuralDecompositionProvider decompositionProvider =
                    factory.createDecompositionProvider("deepseek-chat");
            assertThat(decompositionProvider)
                    .isInstanceOf(ConfigurableStructuralDecompositionProvider.class);
            ConfigurableStructuralDecompositionProvider
                    configurableStructuralDecompositionProvider =
                            (ConfigurableStructuralDecompositionProvider) decompositionProvider;
            assertThat(configurableStructuralDecompositionProvider.singleCharConfig()).isPresent();
            assertThat(configurableStructuralDecompositionProvider.multiCharConfig()).isPresent();
        } finally {
            if (previous == null) {
                System.clearProperty("DEEPSEEK_API_KEY");
            } else {
                System.setProperty("DEEPSEEK_API_KEY", previous);
            }
        }
    }
}
