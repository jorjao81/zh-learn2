package com.zhlearn.domain.model;

public record WordAnalysis(
    ChineseWord word,
    Pinyin pinyin,
    Definition definition,
    StructuralDecomposition structuralDecomposition,
    Example examples,
    Explanation explanation,
    String providerName
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
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }
    }
}