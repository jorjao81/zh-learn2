package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Definition;

import java.util.function.Function;

public final class SingleCharDefinitionFormatterProviderConfig {

    private static final String TEMPLATE_PATH = "/single-char/definition/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/single-char/definition/examples/";
    private static final Function<String, Definition> RESPONSE_MAPPER = new DefinitionResponseMapper();
    private static final Double DEFAULT_TEMPERATURE = 0.3;
    private static final Integer DEFAULT_MAX_TOKENS = 4000;

    private SingleCharDefinitionFormatterProviderConfig() {
    }

    public static String templatePath() {
        return TEMPLATE_PATH;
    }

    public static String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public static Function<String, Definition> responseMapper() {
        return RESPONSE_MAPPER;
    }

    public static Double defaultTemperature() {
        return DEFAULT_TEMPERATURE;
    }

    public static Integer defaultMaxTokens() {
        return DEFAULT_MAX_TOKENS;
    }
}