package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Definition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class DefinitionResponseMapper implements Function<String, Definition> {

    private static final Logger log = LoggerFactory.getLogger(DefinitionResponseMapper.class);

    @Override
    public Definition apply(String response) {
        try {
            // Strip markdown code blocks if present
            String cleanedResponse = stripMarkdownCodeBlocks(response);

            // Trim whitespace and return as Definition
            String trimmed = cleanedResponse.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalStateException("Empty definition response");
            }

            return new Definition(trimmed);

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.warn("Failed to parse definition response: {}", errorMessage);
            log.debug("Original response: {}", response);
            throw new RuntimeException("Failed to parse definition response: " + errorMessage, e);
        }
    }

    private String stripMarkdownCodeBlocks(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();

        // Check if the input starts with ```html or ``` and ends with ```
        if (trimmed.startsWith("```html") && trimmed.endsWith("```")) {
            // Remove ```html from the start and ``` from the end
            int startIndex = trimmed.indexOf('\n', 7); // Find first newline after ```html
            if (startIndex == -1) {
                startIndex = 7; // No newline found, start after ```html
            } else {
                startIndex++; // Move past the newline
            }
            int endIndex = trimmed.lastIndexOf("```");
            return trimmed.substring(startIndex, endIndex).trim();
        } else if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            // Generic code block without html specifier
            int startIndex = trimmed.indexOf('\n', 3); // Find first newline after ```
            if (startIndex == -1) {
                startIndex = 3; // No newline found, start after ```
            } else {
                startIndex++; // Move past the newline
            }
            int endIndex = trimmed.lastIndexOf("```");
            return trimmed.substring(startIndex, endIndex).trim();
        }

        // No markdown code blocks found, return as is
        return input;
    }
}