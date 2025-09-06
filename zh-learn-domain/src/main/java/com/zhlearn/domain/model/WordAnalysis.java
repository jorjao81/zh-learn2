package com.zhlearn.domain.model;

import java.util.Optional;

public record WordAnalysis(
    Hanzi word,
    Pinyin pinyin,
    Definition definition,
    StructuralDecomposition structuralDecomposition,
    Example examples,
    Explanation explanation,
    Optional<String> pronunciation
) {
    public WordAnalysis {
        if (word == null) {
            throw new IllegalArgumentException("Chinese word cannot be null");
        }
        if (pinyin == null) {
            throw new IllegalArgumentException("Pinyin cannot be null");
        }
        if (definition == null) {
            throw new IllegalArgumentException("Definition cannot be null");
        }
        if (structuralDecomposition == null) {
            throw new IllegalArgumentException("Structural decomposition cannot be null");
        }
        if (examples == null) {
            throw new IllegalArgumentException("Examples cannot be null");
        }
        if (explanation == null) {
            throw new IllegalArgumentException("Explanation cannot be null");
        }
        if (pronunciation == null) {
            throw new IllegalArgumentException("Pronunciation cannot be null");
        }
    }
}