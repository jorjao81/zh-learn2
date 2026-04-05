package com.zhlearn.infrastructure.explanation;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.zhlearn.domain.model.ExplanationEntry;

/**
 * Exporter that generates Anki-compatible TSV files from parsed explanation entries. Produces a
 * "Chinese Explanation" note type with Simplified, Pinyin, Explanation, EntryType, and Tags
 * columns. The Explanation field stores raw markdown, rendered client-side by marked.js in Anki.
 */
public class ExplanationTsvExporter {

    /**
     * Export entries to an Anki TSV file.
     *
     * @param entries the parsed entries
     * @param outputPath where to write the TSV
     * @throws IOException if file cannot be written
     */
    public void exportToTsv(List<ExplanationEntry> entries, Path outputPath) throws IOException {
        try (PrintWriter writer =
                new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))) {
            writeHeaders(writer);
            for (ExplanationEntry entry : entries) {
                writeEntry(writer, entry);
            }
        }
    }

    private void writeHeaders(PrintWriter writer) {
        writer.println("#separator:Tab");
        writer.println("#html:true");
        writer.println("#notetype:Chinese Explanation");
        writer.println("#columns:Simplified\tPinyin\tExplanation\tEntryType\tTags");
    }

    private void writeEntry(PrintWriter writer, ExplanationEntry entry) {
        String line =
                String.join(
                        "\t",
                        escapeForTsv(entry.term()),
                        escapeForTsv(entry.pinyin()),
                        escapeForTsv(entry.explanation()),
                        escapeForTsv(entry.entryType()),
                        escapeForTsv(entry.entryType()) // Tags = entryType
                        );
        writer.println(line);
    }

    /** Escape a string value for safe inclusion in TSV format. */
    private String escapeForTsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("\t")
                || value.contains("\n")
                || value.contains("\r")
                || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
