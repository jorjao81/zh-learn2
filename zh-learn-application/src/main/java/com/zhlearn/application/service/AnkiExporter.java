package com.zhlearn.application.service;

import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.application.export.AnkiExportEntry;
import com.zhlearn.domain.model.WordAnalysis;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Service for exporting WordAnalysis results to Anki-compatible TSV format.
 * Generates files suitable for import into Anki using the "Chinese 2" note type.
 */
public class AnkiExporter {

    /**
     * Export a list of WordAnalysis objects to an Anki-compatible TSV file.
     * Each analysis is converted to an AnkiExportEntry and written as a tab-separated line.
     */
    public void exportToFile(List<WordAnalysis> analyses, Path outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {
            // Write Anki TSV headers
            writeAnkiHeaders(writer);
            
            // Write data rows
            for (WordAnalysis analysis : analyses) {
                String examplesHtml = ExamplesHtmlFormatter.format(analysis.examples());
                AnkiExportEntry entry = new AnkiExportEntry(
                    "Chinese 2",
                    analysis.word().characters(),
                    analysis.pinyin().pinyin(),
                    analysis.pronunciation().orElse(""),
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
