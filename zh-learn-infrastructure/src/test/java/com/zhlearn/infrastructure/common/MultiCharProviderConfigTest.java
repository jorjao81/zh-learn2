package com.zhlearn.infrastructure.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiCharProviderConfigTest {

    @Test
    void exampleConfigShouldExposeMultiCharPaths() {
        assertThat(MultiCharExampleProviderConfig.templatePath())
            .isEqualTo("/multi-char/examples/prompt-template.md");
        assertThat(MultiCharExampleProviderConfig.examplesDirectory())
            .isEqualTo("/multi-char/examples/examples/");
        assertThat(MultiCharExampleProviderConfig.responseMapper())
            .isNotNull();
    }

    @Test
    void explanationConfigShouldExposeMultiCharPaths() {
        assertThat(MultiCharExplanationProviderConfig.templatePath())
            .isEqualTo("/multi-char/explanation/prompt-template.md");
        assertThat(MultiCharExplanationProviderConfig.examplesDirectory())
            .isEqualTo("/multi-char/explanation/examples/");
        assertThat(MultiCharExplanationProviderConfig.responseMapper())
            .isNotNull();
    }

    @Test
    void structuralConfigShouldExposeMultiCharPaths() {
        assertThat(MultiCharStructuralDecompositionProviderConfig.templatePath())
            .isEqualTo("/multi-char/structural-decomposition/prompt-template.md");
        assertThat(MultiCharStructuralDecompositionProviderConfig.examplesDirectory())
            .isEqualTo("/multi-char/structural-decomposition/examples/");
        assertThat(MultiCharStructuralDecompositionProviderConfig.responseMapper())
            .isNotNull();
    }
}
