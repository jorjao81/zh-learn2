package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;

import java.util.function.Function;

public final class MultiCharExampleProviderConfig {

    private static final String TEMPLATE_PATH = "/multi-char/examples/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/multi-char/examples/examples/";
    private static final Function<String, Example> RESPONSE_MAPPER = new MultiCharExampleResponseMapper();
    private static final Double DEFAULT_TEMPERATURE = 0.3;
    private static final Integer DEFAULT_MAX_TOKENS = 8000;

    private MultiCharExampleProviderConfig() {
    }

    public static String templatePath() {
        return TEMPLATE_PATH;
    }

    public static String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public static Function<String, Example> responseMapper() {
        return RESPONSE_MAPPER;
    }

    public static Double defaultTemperature() {
        return DEFAULT_TEMPERATURE;
    }

    public static Integer defaultMaxTokens() {
        return DEFAULT_MAX_TOKENS;
    }
}
