package com.zhlearn.infrastructure.explanation;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhlearn.domain.model.ExplanationEntry;

/**
 * Exporter that generates Anki-compatible TSV files from parsed explanation entries. Produces a
 * "Chinese Explanation" note type with Simplified, Pinyin, Audio, Explanation, EntryType, and Tags
 * columns. The Explanation field stores raw markdown, rendered client-side by marked.js in Anki.
 */
public class ExplanationTsvExporter {

    private static final Logger log = LoggerFactory.getLogger(ExplanationTsvExporter.class);

    /**
     * Export entries to an Anki TSV file, copying audio files to the Anki media directory.
     *
     * @param entries the parsed entries
     * @param outputPath where to write the TSV
     * @param markdownDir the directory containing the markdown (audio paths are relative to this)
     * @param ankiMediaDir the Anki media directory to copy audio files into, or null to skip
     * @throws IOException if file cannot be written
     */
    public void exportToTsv(
            List<ExplanationEntry> entries, Path outputPath, Path markdownDir, Path ankiMediaDir)
            throws IOException {
        try (PrintWriter writer =
                new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))) {
            writeHeaders(writer);
            for (ExplanationEntry entry : entries) {
                String audioRef = copyAudioAndGetRef(entry, markdownDir, ankiMediaDir);
                writeEntry(writer, entry, audioRef);
            }
        }
    }

    private void writeHeaders(PrintWriter writer) {
        writer.println("#separator:Tab");
        writer.println("#html:true");
        writer.println("#notetype:Chinese Explanation");
        writer.println("#columns:Simplified\tPinyin\tAudio\tExplanation\tEntryType\tTags");
    }

    private void writeEntry(PrintWriter writer, ExplanationEntry entry, String audioRef) {
        String line =
                String.join(
                        "\t",
                        escapeForTsv(entry.term()),
                        escapeForTsv(entry.pinyin()),
                        escapeForTsv(audioRef),
                        escapeForTsv(entry.explanation()),
                        escapeForTsv(entry.entryType()),
                        escapeForTsv(entry.entryType()) // Tags = entryType
                        );
        writer.println(line);
    }

    /**
     * Copy the audio file to Anki media dir and return the [sound:...] reference. Returns empty
     * string if entry has no audio or copy fails.
     */
    private String copyAudioAndGetRef(ExplanationEntry entry, Path markdownDir, Path ankiMediaDir) {
        if (entry.audioPath() == null) {
            return "";
        }

        Path audioSource = markdownDir.resolve(entry.audioPath());
        if (!Files.isRegularFile(audioSource)) {
            log.warn("[Export] Audio file not found for '{}': {}", entry.term(), audioSource);
            return "";
        }

        String fileName = audioSource.getFileName().toString();

        if (ankiMediaDir != null) {
            Path target = ankiMediaDir.resolve(fileName);
            try {
                Files.copy(audioSource, target, StandardCopyOption.REPLACE_EXISTING);
                log.info("[Export] Copied audio to Anki media: {}", fileName);
            } catch (IOException e) {
                log.warn(
                        "[Export] Failed to copy audio for '{}': {}", entry.term(), e.getMessage());
                return "";
            }
        }

        return "[sound:" + fileName + "]";
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
