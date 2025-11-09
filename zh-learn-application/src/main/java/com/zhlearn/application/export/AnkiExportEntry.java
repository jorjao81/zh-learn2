package com.zhlearn.application.export;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.zhlearn.application.format.DefinitionImageFormatter;

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
        String noHearing,
        Optional<List<Path>> images) {
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
        if (images == null) images = Optional.empty();
    }

    /**
     * Get the formatted definition with embedded images if present.
     *
     * @return definition formatted with HTML image tags if images are present, otherwise plain
     *     definition
     */
    public String formattedDefinition() {
        if (images.isPresent() && !images.get().isEmpty()) {
            DefinitionImageFormatter formatter = new DefinitionImageFormatter();
            return formatter.formatWithImages(definition, images.get());
        }
        return definition;
    }
}
