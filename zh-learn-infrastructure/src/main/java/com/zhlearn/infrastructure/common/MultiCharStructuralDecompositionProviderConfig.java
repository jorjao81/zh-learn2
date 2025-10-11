package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.StructuralDecomposition;

public class MultiCharStructuralDecompositionProviderConfig {

    private static final String TEMPLATE_PATH =
            "/multi-char/structural-decomposition/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY =
            "/multi-char/structural-decomposition/examples/";
    private static final Function<String, StructuralDecomposition> RESPONSE_MAPPER =
            StructuralDecomposition::new;

    public MultiCharStructuralDecompositionProviderConfig() {}

    public String templatePath() {
        return TEMPLATE_PATH;
    }

    public String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public Function<String, StructuralDecomposition> responseMapper() {
        return RESPONSE_MAPPER;
    }
}
