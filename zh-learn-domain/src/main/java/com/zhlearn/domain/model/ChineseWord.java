package com.zhlearn.domain.model;

public record ChineseWord(String characters) {
    public ChineseWord {
        if (characters == null || characters.trim().isEmpty()) {
            throw new IllegalArgumentException("Chinese word characters cannot be null or empty");
        }
    }
}