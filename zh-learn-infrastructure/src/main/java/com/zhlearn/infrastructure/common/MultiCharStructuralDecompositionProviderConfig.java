package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.StructuralDecomposition;

import java.util.function.Function;

public final class MultiCharStructuralDecompositionProviderConfig {

    private static final String TEMPLATE_PATH = "/multi-char/structural-decomposition/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/multi-char/structural-decomposition/examples/";
    private static final Function<String, StructuralDecomposition> RESPONSE_MAPPER = StructuralDecomposition::new;
    private static final Double DEFAULT_TEMPERATURE = 0.3;
    private static final Integer DEFAULT_MAX_TOKENS = 8000;

    private MultiCharStructuralDecompositionProviderConfig() {
    }

    public static String templatePath() {
        return TEMPLATE_PATH;
    }

    public static String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public static Function<String, StructuralDecomposition> responseMapper() {
        return RESPONSE_MAPPER;
    }

    public static Double defaultTemperature() {
        return DEFAULT_TEMPERATURE;
    }

    public static Integer defaultMaxTokens() {
        return DEFAULT_MAX_TOKENS;
    }
}

