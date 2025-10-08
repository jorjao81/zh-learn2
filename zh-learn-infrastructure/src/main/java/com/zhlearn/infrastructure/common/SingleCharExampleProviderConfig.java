package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Example;

public class SingleCharExampleProviderConfig {

    private static final String TEMPLATE_PATH = "/single-char/examples/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/single-char/examples/examples/";
    private static final Function<String, Example> RESPONSE_MAPPER = new ExampleResponseMapper();
    private static final Double DEFAULT_TEMPERATURE = 0.3;
    private static final Integer DEFAULT_MAX_TOKENS = 8000;

    public SingleCharExampleProviderConfig() {}

    public String templatePath() {
        return TEMPLATE_PATH;
    }

    public String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public Function<String, Example> responseMapper() {
        return RESPONSE_MAPPER;
    }

    public Double defaultTemperature() {
        return DEFAULT_TEMPERATURE;
    }

    public Integer defaultMaxTokens() {
        return DEFAULT_MAX_TOKENS;
    }
}
