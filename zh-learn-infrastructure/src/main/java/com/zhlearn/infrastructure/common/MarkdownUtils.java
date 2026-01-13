package com.zhlearn.infrastructure.common;

/** Utility class for processing markdown-formatted text from LLM responses. */
public final class MarkdownUtils {

    private MarkdownUtils() {}

    /**
     * Strips markdown code block delimiters from input text. Handles code blocks with or without
     * language specifiers (e.g., ```yaml, ```html, or plain ```).
     *
     * @param input the text potentially wrapped in markdown code blocks
     * @return the content inside the code blocks, or the original input if no blocks found
     */
    public static String stripCodeBlocks(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();

        // Check if wrapped in code blocks
        if (!trimmed.startsWith("```") || !trimmed.endsWith("```")) {
            return trimmed;
        }

        // Find where the opening ``` line ends
        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline == -1) {
            // Malformed: no newline after opening ```
            return trimmed;
        }

        // Find the closing ```
        int closingIndex = trimmed.lastIndexOf("```");
        if (closingIndex <= firstNewline) {
            // Malformed: closing ``` is before or at the content start
            return trimmed;
        }

        // Extract content between opening line and closing ```
        return trimmed.substring(firstNewline + 1, closingIndex).trim();
    }
}
