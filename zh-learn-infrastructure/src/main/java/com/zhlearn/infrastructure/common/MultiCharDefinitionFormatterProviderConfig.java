package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Definition;

public class MultiCharDefinitionFormatterProviderConfig {

    private static final String TEMPLATE_PATH = "/multi-char/definition/prompt-template.md";
    private static final String EXAMPLES_DIRECTORY = "/multi-char/definition/examples/";
    private static final Function<String, Definition> RESPONSE_MAPPER =
            new DefinitionResponseMapper();

    public MultiCharDefinitionFormatterProviderConfig() {}

    public String templatePath() {
        return TEMPLATE_PATH;
    }

    public String examplesDirectory() {
        return EXAMPLES_DIRECTORY;
    }

    public Function<String, Definition> responseMapper() {
        return RESPONSE_MAPPER;
    }
}
