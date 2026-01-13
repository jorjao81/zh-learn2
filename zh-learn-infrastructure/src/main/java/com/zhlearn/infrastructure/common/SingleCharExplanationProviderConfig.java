package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Explanation;

public class SingleCharExplanationProviderConfig {

    private static final String TEMPLATE_PATH = "/single-char/explanation/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/single-char/explanation/examples/";
    private static final Function<String, Explanation> RESPONSE_MAPPER = Explanation::new;
    private static final Double DEFAULT_TEMPERATURE = 0.3;
    private static final Integer DEFAULT_MAX_TOKENS = 8000;

    public SingleCharExplanationProviderConfig() {}

    public String templatePath() {
        return TEMPLATE_PATH;
    }

    public String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public Function<String, Explanation> responseMapper() {
        return RESPONSE_MAPPER;
    }

    public Double defaultTemperature() {
        return DEFAULT_TEMPERATURE;
    }

    public Integer defaultMaxTokens() {
        return DEFAULT_MAX_TOKENS;
    }
}
