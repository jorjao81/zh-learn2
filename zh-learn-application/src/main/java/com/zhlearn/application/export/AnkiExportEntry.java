package com.zhlearn.application.export;

/**
 * Represents a single Anki note entry for export in "Chinese 2" format. Contains all the fields
 * needed for Anki TSV import.
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
        String noHearing) {
    public AnkiExportEntry {
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
     * Get the formatted definition.
     *
     * @return definition text
     */
    public String formattedDefinition() {
        return definition;
    }
}
