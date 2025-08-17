package com.zhlearn.domain.model;

import java.util.List;

public record Explanation(
    String etymology, 
    String usage, 
    List<String> similarWords, 
    String culturalContext
) {
    public Explanation {
        if (etymology == null || etymology.trim().isEmpty()) {
            throw new IllegalArgumentException("Etymology cannot be null or empty");
        }
        if (usage == null || usage.trim().isEmpty()) {
            throw new IllegalArgumentException("Usage explanation cannot be null or empty");
        }
        if (similarWords == null) {
            throw new IllegalArgumentException("Similar words list cannot be null");
        }
    }
}