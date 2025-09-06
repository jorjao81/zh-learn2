package com.zhlearn.infrastructure.anki;

/**
 * Represents a row from the full Anki collection TSV (Chinese.txt),
 * where the first column is the Note Type (e.g., "Chinese" or "Chinese 2").
 */
public record AnkiCollectionNote(
    String noteType,
    String simplified,
    String pinyin,
    String pronunciation,
    String definition,
    String examples,
    String etymology,
    String components
) {
}

