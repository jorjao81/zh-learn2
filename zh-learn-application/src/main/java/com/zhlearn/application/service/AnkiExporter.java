package com.zhlearn.application.service;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.application.export.AnkiExportEntry;
import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.domain.model.WordAnalysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

/**
 * Service for exporting WordAnalysis results to Anki-compatible TSV format.
 * Generates files suitable for import into Anki using the "Chinese 2" note type.
 */
public class AnkiExporter {
    private final ExamplesHtmlFormatter examplesHtmlFormatter;
    private final AnkiMediaLocator ankiMediaLocator;

    public AnkiExporter(ExamplesHtmlFormatter examplesHtmlFormatter, AnkiMediaLocator ankiMediaLocator) {
        this.examplesHtmlFormatter = examplesHtmlFormatter;
        this.ankiMediaLocator = ankiMediaLocator;
    }

    /**
     * Export a list of WordAnalysis objects to an Anki-compatible TSV file.
     * Each analysis is converted to an AnkiExportEntry and written as a tab-separated line.
     */
    public void exportToFile(List<WordAnalysis> analyses, Path outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {
            // Write Anki TSV headers
            writeAnkiHeaders(writer);
            Optional<Path> ankiMediaDir = ankiMediaLocator.locate();

            // Write data rows
            for (WordAnalysis analysis : analyses) {
                String examplesHtml = examplesHtmlFormatter.format(analysis.examples());
                String soundNotation = buildSoundNotation(analysis.pronunciation(), ankiMediaDir);

                AnkiExportEntry entry = new AnkiExportEntry(
                    "Chinese 2",
                    analysis.word().characters(),
                    analysis.pinyin().pinyin(),
                    soundNotation,
                    analysis.definition().meaning(),
                    examplesHtml,
                    analysis.explanation().explanation(),
                    analysis.structuralDecomposition().decomposition(),
                    "", // similar
                    "y", // passive
                    "", // alternatePronunciations
                    "y"  // noHearing
                );
                writer.println(formatAsTabSeparated(entry));
            }
        }
    }

    /**
     * Export a list of WordAnalysis objects to an Anki-compatible TSV file.
     * Convenience method that takes a string filename.
     */
    public void exportToFile(List<WordAnalysis> analyses, String filename) throws IOException {
        exportToFile(analyses, Path.of(filename));
    }

    private String buildSoundNotation(Optional<Path> pronunciation, Optional<Path> ankiMediaDir) throws IOException {
        if (pronunciation.isEmpty()) {
            return "";
        }
        Path target = ensureAudioInAnkiMedia(pronunciation.get(), ankiMediaDir);
        Path fileName = target.getFileName();
        if (fileName == null) {
            throw new IOException("Unable to derive filename for pronunciation audio: " + target);
        }
        return "[sound:" + fileName.toString() + "]";
    }

    private Path ensureAudioInAnkiMedia(Path audioFile, Optional<Path> ankiMediaDir) throws IOException {
        Path source = audioFile.toAbsolutePath().normalize();
        if (!Files.exists(source)) {
            throw new IOException("Pronunciation audio file not found: " + source);
        }

        if (ankiMediaDir.isEmpty()) {
            return source;
        }

        Path mediaDir = ankiMediaDir.get().toAbsolutePath().normalize();
        Files.createDirectories(mediaDir);

        if (source.startsWith(mediaDir)) {
            return source;
        }

        Path target = mediaDir.resolve(source.getFileName());
        if (Files.exists(target)) {
            if (Files.isSameFile(source, target)) {
                return target.toAbsolutePath();
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath();
        }

        Files.copy(source, target);
        return target.toAbsolutePath();
    }

    /**
     * Format an AnkiExportEntry as a tab-separated line for TSV output.
     * Properly escapes special characters that could break TSV format.
     */
    private String formatAsTabSeparated(AnkiExportEntry entry) {
        return String.join("\t",
            escapeForTSV(entry.noteType()),
            escapeForTSV(entry.simplified()),
            escapeForTSV(entry.pinyin()),
            escapeForTSV(entry.pronunciation()),
            escapeForTSV(entry.definition()),
            escapeForTSV(entry.examples()),
            escapeForTSV(entry.etymology()),
            escapeForTSV(entry.components()),
            escapeForTSV(entry.similar()),
            escapeForTSV(entry.passive()),
            escapeForTSV(entry.alternatePronunciations()),
            escapeForTSV(entry.noHearing())
        );
    }

    /**
     * Escape a string value for safe inclusion in TSV format.
     * Handles tabs, newlines, and quotes that could break the format.
     */
    private String escapeForTSV(String value) {
        if (value == null) {
            return "";
        }

        // If the value contains tabs, newlines, or quotes, we need to quote it
        if (value.contains("\t") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
            // Escape quotes by doubling them, then wrap in quotes
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
    
    /**
     * Write the required Anki TSV header directives.
     * These headers help Anki understand the file format during import.
     */
    private void writeAnkiHeaders(PrintWriter writer) {
        writer.println("#separator:tab");
        writer.println("#html:true");
        writer.println("#notetype column:1");
    }
}
