package com.zhlearn.domain.model;

public record WordAnalysis(
    Hanzi word,
    Pinyin pinyin,
    Definition definition,
    StructuralDecomposition structuralDecomposition,
    Example examples,
    Explanation explanation,
    String providerName,
    String pinyinProvider,
    String definitionProvider,
    String decompositionProvider,
    String exampleProvider,
    String explanationProvider
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
        if (pinyinProvider == null || pinyinProvider.trim().isEmpty()) {
            throw new IllegalArgumentException("Pinyin provider cannot be null or empty");
        }
        if (definitionProvider == null || definitionProvider.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition provider cannot be null or empty");
        }
        if (decompositionProvider == null || decompositionProvider.trim().isEmpty()) {
            throw new IllegalArgumentException("Decomposition provider cannot be null or empty");
        }
        if (exampleProvider == null || exampleProvider.trim().isEmpty()) {
            throw new IllegalArgumentException("Example provider cannot be null or empty");
        }
        if (explanationProvider == null || explanationProvider.trim().isEmpty()) {
            throw new IllegalArgumentException("Explanation provider cannot be null or empty");
        }
    }
}