package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Definition;

public class MultiCharDefinitionGeneratorProviderConfig {

    private static final String TEMPLATE_PATH =
            "/multi-char/definition-generator/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/multi-char/definition-generator/examples/";
    private static final Function<String, Definition> RESPONSE_MAPPER =
            new DefinitionResponseMapper();
    private static final Double DEFAULT_TEMPERATURE = 0.3;

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
}
