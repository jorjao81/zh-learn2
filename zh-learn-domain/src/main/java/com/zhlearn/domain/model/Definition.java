package com.zhlearn.domain.model;

public record Definition(String meaning, String partOfSpeech) {
    public Definition {
        if (meaning == null || meaning.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition meaning cannot be null or empty");
        }
        if (partOfSpeech == null || partOfSpeech.trim().isEmpty()) {
            throw new IllegalArgumentException("Part of speech cannot be null or empty");
        }
    }
}