package com.zhlearn.domain.model;

public record Definition(String meaning) {
    public Definition {
        if (meaning == null || meaning.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition meaning cannot be null or empty");
        }
    }
}
