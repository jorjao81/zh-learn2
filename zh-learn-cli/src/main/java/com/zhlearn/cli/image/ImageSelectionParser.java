package com.zhlearn.cli.image;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses pre-configured image selection strings in the format "word:1,2,3". */
public final class ImageSelectionParser {

    // Pattern to match "word:indices" where word can contain any characters except : and indices
    // are comma-separated numbers (with optional spaces)
    private static final Pattern SELECTION_PATTERN = Pattern.compile("([^:\\s]+):\\s*([\\d,\\s]+)");

    private ImageSelectionParser() {
        // Utility class
    }

    /**
     * Parse image selection configuration string.
     *
     * @param input selection string in format "word:1,2,3" or "word1:1,2 word2:3,4"
     * @return map of word to selected image indices (1-based)
     * @throws IllegalArgumentException if format is invalid
     */
    public static Map<String, List<Integer>> parse(String input) {
        if (input == null || input.isBlank()) {
            return Map.of();
        }

        Map<String, List<Integer>> result = new LinkedHashMap<>();
        Matcher matcher = SELECTION_PATTERN.matcher(input.trim());

        while (matcher.find()) {
            String word = matcher.group(1).trim();
            String indicesStr = matcher.group(2).trim();

            if (word.isEmpty()) {
                throw new IllegalArgumentException("Invalid image selection: word cannot be empty");
            }

            if (indicesStr.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid image selection: indices cannot be empty for word '" + word + "'");
            }

            // Parse comma-separated indices
            try {
                List<Integer> indices =
                        Arrays.stream(indicesStr.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(Integer::parseInt)
                                .filter(i -> i > 0) // Only positive indices
                                .toList();

                if (indices.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Invalid image selection: no valid positive indices for word '"
                                    + word
                                    + "'");
                }

                result.put(word, indices);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid image selection: indices must be numbers for word '"
                                + word
                                + "': "
                                + indicesStr,
                        e);
            }
        }

        return result;
    }
}
