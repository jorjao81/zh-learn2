package com.zhlearn.domain.model;

// TODO: Come up with a proper model for StructuralDecomposition
public record StructuralDecomposition(String decomposition) {
    public StructuralDecomposition {
        if (decomposition == null || decomposition.trim().isEmpty()) {
            throw new IllegalArgumentException("Structural decomposition cannot be null or empty");
        }
    }
}
