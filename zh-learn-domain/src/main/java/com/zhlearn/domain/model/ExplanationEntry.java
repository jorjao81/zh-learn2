package com.zhlearn.domain.model;

/** Represents a parsed entry from a Chinese explanation markdown file. */
public record ExplanationEntry(String term, String pinyin, String explanation, String entryType) {

    public ExplanationEntry {
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("Term cannot be null or blank");
        }
        if (pinyin == null || pinyin.isBlank()) {
            throw new IllegalArgumentException("Pinyin cannot be null or blank");
        }
        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("Explanation cannot be null or blank");
        }
        if (entryType == null || entryType.isBlank()) {
            throw new IllegalArgumentException("Entry type cannot be null or blank");
        }
    }
}
