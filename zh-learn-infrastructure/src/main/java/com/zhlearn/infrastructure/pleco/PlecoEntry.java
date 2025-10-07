package com.zhlearn.infrastructure.pleco;

/**
 * Represents a single entry from a Pleco export file. Each entry contains the Chinese characters,
 * pinyin (converted to tone marks), and definition text as-is from the export.
 */
public record PlecoEntry(String hanzi, String pinyin, String definitionText) {

    /**
     * Create a PlecoEntry with validation.
     *
     * @param hanzi the Chinese characters
     * @param pinyin the pinyin with tone marks
     * @param definitionText the definition text (not parsed)
     */
    public PlecoEntry {
        if (hanzi == null || hanzi.trim().isEmpty()) {
            throw new IllegalArgumentException("hanzi cannot be null or empty");
        }
        if (pinyin == null || pinyin.trim().isEmpty()) {
            throw new IllegalArgumentException("pinyin cannot be null or empty");
        }
        if (definitionText == null) {
            definitionText = "";
        }

        // Trim all fields
        hanzi = hanzi.trim();
        pinyin = pinyin.trim();
        definitionText = definitionText.trim();
    }
}
