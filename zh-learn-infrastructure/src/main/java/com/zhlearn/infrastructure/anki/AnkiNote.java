package com.zhlearn.infrastructure.anki;

/**
 * Unified model for Anki notes parsed from the Collection TSV (Chinese.txt).
 * Column order (observed for note type "Chinese 2"):
 * 0: noteType, 1: simplified, 2: pinyin, 3: pronunciation, 4: definition,
 * 5: examples, 6: etymology, 7: components, 8+: optional fields
 */
public record AnkiNote(
    String noteType,
    String pinyin,
    String simplified,
    String pronunciation,
    String definition,
    String examples,
    String etymology,
    String components,
    String similar,
    String passive,
    String alternatePronunciations,
    String noHearing
) {
    public static AnkiNote ofCollection(
        String noteType,
        String pinyin,
        String simplified,
        String pronunciation,
        String definition,
        String examples,
        String etymology,
        String components,
        String similar,
        String passive,
        String alternatePronunciations,
        String noHearing
    ) {
        return new AnkiNote(
            safe(noteType), safe(pinyin), safe(simplified), safe(pronunciation),
            safe(definition), safe(examples), safe(etymology), safe(components),
            safe(similar), safe(passive), safe(alternatePronunciations), safe(noHearing)
        );
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
