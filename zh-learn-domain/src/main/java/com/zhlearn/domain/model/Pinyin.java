package com.zhlearn.domain.model;

public record Pinyin(String romanization, String toneMarks) {
    public Pinyin {
        if (romanization == null || romanization.trim().isEmpty()) {
            throw new IllegalArgumentException("Pinyin romanization cannot be null or empty");
        }
        if (toneMarks == null || toneMarks.trim().isEmpty()) {
            throw new IllegalArgumentException("Pinyin tone marks cannot be null or empty");
        }
    }
}