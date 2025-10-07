package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.StructuralDecomposition;

public class SingleCharStructuralDecompositionProviderConfig {

    private static final String TEMPLATE_PATH =
            "/single-char/structural-decomposition/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY =
            "/single-char/structural-decomposition/examples/";
    private static final Function<String, StructuralDecomposition> RESPONSE_MAPPER =
            StructuralDecomposition::new;
    private static final Double DEFAULT_TEMPERATURE = 0.3;
    private static final Integer DEFAULT_MAX_TOKENS = 8000;

    public SingleCharStructuralDecompositionProviderConfig() {}

    public String templatePath() {
        return TEMPLATE_PATH;
    }

    public String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public Function<String, StructuralDecomposition> responseMapper() {
        return RESPONSE_MAPPER;
    }

    public Double defaultTemperature() {
        return DEFAULT_TEMPERATURE;
    }

    public Integer defaultMaxTokens() {
        return DEFAULT_MAX_TOKENS;
    }
}
