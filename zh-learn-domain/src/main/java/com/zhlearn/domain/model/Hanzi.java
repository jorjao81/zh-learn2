package com.zhlearn.domain.model;

public record Hanzi(String characters) {
    public Hanzi {
        if (characters == null || characters.trim().isEmpty()) {
            throw new IllegalArgumentException("Chinese word characters cannot be null or empty");
        }
    }

    public boolean isSingleCharacter() {
        int codePointCount = characters.codePointCount(0, characters.length());
        return codePointCount == 1;
    }

    public boolean isMultiCharacter() {
        return !isSingleCharacter();
    }
}
