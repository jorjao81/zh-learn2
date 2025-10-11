package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Explanation;

public class MultiCharExplanationProviderConfig {

    private static final String TEMPLATE_PATH = "/multi-char/explanation/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/multi-char/explanation/examples/";
    private static final Function<String, Explanation> RESPONSE_MAPPER = Explanation::new;

    public MultiCharExplanationProviderConfig() {}

    public String templatePath() {
        return TEMPLATE_PATH;
    }

    public String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public Function<String, Explanation> responseMapper() {
        return RESPONSE_MAPPER;
    }
}
