package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Example;

public class MultiCharExampleProviderConfig {

    private static final String TEMPLATE_PATH = "/multi-char/examples/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/multi-char/examples/examples/";
    private static final Function<String, Example> RESPONSE_MAPPER =
            new MultiCharExampleResponseMapper();

    public MultiCharExampleProviderConfig() {}

    public String templatePath() {
        return TEMPLATE_PATH;
    }

    public String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public Function<String, Example> responseMapper() {
        return RESPONSE_MAPPER;
    }
}
