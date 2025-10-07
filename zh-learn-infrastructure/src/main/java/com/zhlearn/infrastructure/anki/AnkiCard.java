package com.zhlearn.infrastructure.anki;

/**
 * Represents a raw Anki card as parsed from external TSV format. This is an infrastructure model
 * that represents the external system's data structure. All fields are stored as raw strings
 * without any processing.
 */
public record AnkiCard(
        String simplified, // Field 1: Simplified Chinese character
        String pinyin, // Field 2: Pronunciation in pinyin
        String pronunciation, // Field 3: Audio/pronunciation field (raw)
        String definition, // Field 4: Raw definition string
        String examples, // Field 5: Raw examples string (no processing)
        String etymology, // Field 6: Raw etymology string (no processing)
        String components, // Field 7: Raw components string (no processing)
        String similar, // Field 8: Similar characters/words
        String passive, // Field 9: Passive field
        String alternatePronunciations, // Field 10: Alternative pronunciations
        String noHearing // Field 11: Hearing-related field
        ) {

    /**
     * Creates an AnkiCard with null-safe field assignment. Empty or null fields are converted to
     * empty strings.
     */
    public static AnkiCard of(String... fields) {
        String[] safeFields = new String[11];
        for (int i = 0; i < 11; i++) {
            safeFields[i] = (i < fields.length && fields[i] != null) ? fields[i] : "";
        }

        return new AnkiCard(
                safeFields[0], // simplified
                safeFields[1], // pinyin
                safeFields[2], // pronunciation
                safeFields[3], // definition
                safeFields[4], // examples
                safeFields[5], // etymology
                safeFields[6], // components
                safeFields[7], // similar
                safeFields[8], // passive
                safeFields[9], // alternatePronunciations
                safeFields[10] // noHearing
                );
    }
}
