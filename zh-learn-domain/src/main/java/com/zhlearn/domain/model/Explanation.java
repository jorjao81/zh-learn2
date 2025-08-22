package com.zhlearn.domain.model;

public record Explanation(String explanation) {
    public Explanation {
        if (explanation == null || explanation.trim().isEmpty()) {
            throw new IllegalArgumentException("Explanation cannot be null or empty");
        }
    }
}