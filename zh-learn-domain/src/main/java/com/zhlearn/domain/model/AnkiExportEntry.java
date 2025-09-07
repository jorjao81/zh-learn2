package com.zhlearn.domain.model;

/**
 * Represents a single Anki note entry for export in "Chinese 2" format.
 * Contains all the fields needed for Anki TSV import.
 */
public record AnkiExportEntry(
    String noteType,
    String simplified,
    String pinyin,
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
    public AnkiExportEntry {
        // Ensure non-null values for required fields
        if (noteType == null) noteType = "";
        if (simplified == null) simplified = "";
        if (pinyin == null) pinyin = "";
        if (pronunciation == null) pronunciation = "";
        if (definition == null) definition = "";
        if (examples == null) examples = "";
        if (etymology == null) etymology = "";
        if (components == null) components = "";
        if (similar == null) similar = "";
        if (passive == null) passive = "";
        if (alternatePronunciations == null) alternatePronunciations = "";
        if (noHearing == null) noHearing = "";
    }

    /**
     * Create an AnkiExportEntry from a WordAnalysis with the standard "Chinese 2" format.
     * Sets passive and noHearing to "y" as required.
     */
    public static AnkiExportEntry fromWordAnalysis(WordAnalysis analysis) {
        return new AnkiExportEntry(
            "Chinese 2",
            analysis.word().characters(),
            analysis.pinyin().pinyin(),
            analysis.pronunciation().orElse(""), // use actual pronunciation from audio provider
            analysis.definition().meaning(),
            formatExamples(analysis.examples()),
            analysis.explanation().explanation(),
            analysis.structuralDecomposition().decomposition(),
            "", // similar - leave blank
            "y", // passive - always "y" as specified
            "", // alternatePronunciations - leave blank
            "y"  // noHearing - always "y" as specified
        );
    }

    /**
     * Format the examples from the Example model into a string suitable for Anki.
     * Combines sentence and translation, separates multiple examples with semicolons.
     */
    private static String formatExamples(Example examples) {
        if (examples == null || examples.usages() == null || examples.usages().isEmpty()) {
            return "";
        }

        return examples.usages().stream()
            .map(usage -> {
                String sentence = usage.sentence() != null ? usage.sentence() : "";
                String translation = usage.translation() != null ? usage.translation() : "";
                if (sentence.isEmpty()) return translation;
                if (translation.isEmpty()) return sentence;
                return sentence + " (" + translation + ")";
            })
            .filter(s -> !s.isEmpty())
            .reduce((a, b) -> a + "; " + b)
            .orElse("");
    }
}