package com.zhlearn.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MultiCharProviderConfigTest {

    private final MultiCharExampleProviderConfig exampleConfig =
            new MultiCharExampleProviderConfig();
    private final MultiCharExplanationProviderConfig explanationConfig =
            new MultiCharExplanationProviderConfig();
    private final MultiCharStructuralDecompositionProviderConfig structuralConfig =
            new MultiCharStructuralDecompositionProviderConfig();

    @Test
    void exampleConfigShouldExposeMultiCharPaths() {
        assertThat(exampleConfig.templatePath())
                .isEqualTo("/multi-char/examples/prompt-template.md");
        assertThat(exampleConfig.examplesDirectory()).isEqualTo("/multi-char/examples/examples/");
        assertThat(exampleConfig.responseMapper()).isNotNull();
    }

    @Test
    void explanationConfigShouldExposeMultiCharPaths() {
        assertThat(explanationConfig.templatePath())
                .isEqualTo("/multi-char/explanation/prompt-template.md");
        assertThat(explanationConfig.examplesDirectory())
                .isEqualTo("/multi-char/explanation/examples/");
        assertThat(explanationConfig.responseMapper()).isNotNull();
    }

    @Test
    void structuralConfigShouldExposeMultiCharPaths() {
        assertThat(structuralConfig.templatePath())
                .isEqualTo("/multi-char/structural-decomposition/prompt-template.md");
        assertThat(structuralConfig.examplesDirectory())
                .isEqualTo("/multi-char/structural-decomposition/examples/");
        assertThat(structuralConfig.responseMapper()).isNotNull();
    }
}
