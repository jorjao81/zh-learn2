package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Definition;

public class DefinitionResponseMapper implements Function<String, Definition> {

    @Override
    public Definition apply(String response) {
        // Strip markdown code blocks if present
        String cleanedResponse = MarkdownUtils.stripCodeBlocks(response);

        // Trim whitespace and return as Definition
        if (cleanedResponse == null || cleanedResponse.isEmpty()) {
            throw new IllegalStateException("Empty definition response");
        }

        return new Definition(cleanedResponse);
    }
}
