package com.zhlearn.domain.model;

import java.util.List;

public record Example(List<Usage> usages) {
    public Example {
        if (usages == null || usages.isEmpty()) {
            throw new IllegalArgumentException("Example usages cannot be null or empty");
        }
    }

    public record Usage(String sentence, String translation, String context) {
        public Usage {
            if (sentence == null || sentence.trim().isEmpty()) {
                throw new IllegalArgumentException("Usage sentence cannot be null or empty");
            }
            if (translation == null || translation.trim().isEmpty()) {
                throw new IllegalArgumentException("Usage translation cannot be null or empty");
            }
        }
    }
}