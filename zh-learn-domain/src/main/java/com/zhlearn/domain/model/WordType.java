package com.zhlearn.domain.model;

public enum WordType {
    SINGLE_CHARACTER,
    MULTI_CHARACTER;

    public static WordType from(Hanzi word) {
        if (word == null) {
            throw new IllegalArgumentException("Word cannot be null");
        }
        return word.isSingleCharacter() ? SINGLE_CHARACTER : MULTI_CHARACTER;
    }
}

