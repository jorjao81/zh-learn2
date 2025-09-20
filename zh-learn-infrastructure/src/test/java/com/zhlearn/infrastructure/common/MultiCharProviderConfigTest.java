package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiCharProviderConfigTest {

    @Test
    void exampleConfigShouldExposeMultiCharResources() {
        assertThat(MultiCharExampleProviderConfig.templatePath())
            .isEqualTo("/multi-char/examples/prompt-template.md");
        assertThat(MultiCharExampleProviderConfig.examplesDirectory())
            .isEqualTo("/multi-char/examples/examples/");
        assertThat(MultiCharExampleProviderConfig.responseMapper())
            .isInstanceOf(ExampleResponseMapper.class);
    }

    @Test
    void explanationConfigShouldExposeMultiCharResources() {
        assertThat(MultiCharExplanationProviderConfig.templatePath())
            .isEqualTo("/multi-char/explanation/prompt-template.md");
        assertThat(MultiCharExplanationProviderConfig.examplesDirectory())
            .isEqualTo("/multi-char/explanation/examples/");
        Explanation explanation = MultiCharExplanationProviderConfig.responseMapper().apply("detail");
        assertThat(explanation).isEqualTo(new Explanation("detail"));
    }

    @Test
    void structuralConfigShouldExposeMultiCharResources() {
        assertThat(MultiCharStructuralDecompositionProviderConfig.templatePath())
            .isEqualTo("/multi-char/structural-decomposition/prompt-template.md");
        assertThat(MultiCharStructuralDecompositionProviderConfig.examplesDirectory())
            .isEqualTo("/multi-char/structural-decomposition/examples/");
        StructuralDecomposition decomposition = MultiCharStructuralDecompositionProviderConfig.responseMapper().apply("parts");
        assertThat(decomposition).isEqualTo(new StructuralDecomposition("parts"));
    }
}

