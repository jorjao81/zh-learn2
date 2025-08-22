package com.zhlearn.domain.model;

public record Pinyin(String pinyin) {
    public Pinyin {
        if (pinyin == null || pinyin.trim().isEmpty()) {
            throw new IllegalArgumentException("Pinyin cannot be null or empty");
        }
    }
}