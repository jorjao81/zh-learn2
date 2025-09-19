package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Explanation;

import java.util.function.Function;

public class ExplanationProviderConfig {

    public static final String TEMPLATE_PATH = "/explanation/prompt-template.md";
    public static final String EXAMPLES_DIRECTORY = "/explanation/examples/";
    public static final Function<String, Explanation> RESPONSE_MAPPER = Explanation::new;
    public static final Double DEFAULT_TEMPERATURE = 0.3;
    public static final Integer DEFAULT_MAX_TOKENS = 8000;

    public static String templatePath() {
        return TEMPLATE_PATH;
    }

    public static String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public static Function<String, Explanation> responseMapper() {
        return RESPONSE_MAPPER;
    }

    public static Double defaultTemperature() {
        return DEFAULT_TEMPERATURE;
    }

    public static Integer defaultMaxTokens() {
        return DEFAULT_MAX_TOKENS;
    }
}