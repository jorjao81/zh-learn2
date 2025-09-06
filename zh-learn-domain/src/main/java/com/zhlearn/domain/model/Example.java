package com.zhlearn.domain.model;

import java.util.List;

public record Example(List<Usage> usages) {
    public Example {
        if (usages == null) {
            throw new IllegalArgumentException("Example usages cannot be null");
        }
    }

    public record Usage(String sentence, String pinyin, String translation, String context, String breakdown) {
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