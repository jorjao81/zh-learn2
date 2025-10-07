package com.zhlearn.domain.model;

import java.util.List;

public record Example(List<Usage> usages, List<SeriesItem> phoneticSeries) {
    public Example {
        if (usages == null) {
            throw new IllegalArgumentException("Example usages cannot be null");
        }
        if (phoneticSeries == null) {
            throw new IllegalArgumentException(
                    "Phonetic series cannot be null (use empty list if none)");
        }
    }

    public record Usage(
            String sentence, String pinyin, String translation, String context, String breakdown) {
        public Usage {
            if (sentence == null || sentence.trim().isEmpty()) {
                throw new IllegalArgumentException("Usage sentence cannot be null or empty");
            }
            if (translation == null || translation.trim().isEmpty()) {
                throw new IllegalArgumentException("Usage translation cannot be null or empty");
            }
        }
    }

    public record SeriesItem(String hanzi, String pinyin, String meaning) {
        public SeriesItem {
            if (hanzi == null || hanzi.trim().isEmpty()) {
                throw new IllegalArgumentException("Series item hanzi cannot be null or empty");
            }
            if (meaning == null) {
                meaning = "";
            }
        }
    }

    // No standalone sentence support
}
