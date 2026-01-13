package com.zhlearn.infrastructure.grammar;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.zhlearn.domain.model.GrammarSentenceExercise;
import com.zhlearn.domain.model.GrammarSentenceFile;

/**
 * Exporter that generates Anki-compatible TSV files from grammar sentence markdown files. Reads the
 * grammar explanation and character breakdown files and combines them with the sentence exercises.
 */
public class GrammarSentenceTsvExporter {

    /**
     * Export a GrammarSentenceFile to TSV format.
     *
     * @param sentenceFile the parsed sentence file
     * @param basePath the base directory to resolve relative paths (e.g., the sentences directory)
     * @param outputPath where to write the TSV
     * @throws IOException if files cannot be read or written
     */
    public void exportToTsv(GrammarSentenceFile sentenceFile, Path basePath, Path outputPath)
            throws IOException {
        // Read grammar explanation
        Path grammarPath = basePath.resolve(sentenceFile.grammarFilePath()).normalize();
        String grammarExplanation = readFileContent(grammarPath);

        // Read character breakdown if it exists
        String characterBreakdown = "";
        if (sentenceFile.hasCharacterBreakdown()) {
            Path charPath = basePath.resolve(sentenceFile.characterFilePath()).normalize();
            if (Files.exists(charPath)) {
                characterBreakdown = readFileContent(charPath);
            }
        }

        exportToTsv(sentenceFile, grammarExplanation, characterBreakdown, outputPath);
    }

    /**
     * Export a GrammarSentenceFile to TSV format with pre-loaded content.
     *
     * @param sentenceFile the parsed sentence file
     * @param grammarExplanation the grammar explanation markdown content
     * @param characterBreakdown the character breakdown markdown content (can be empty)
     * @param outputPath where to write the TSV
     * @throws IOException if file cannot be written
     */
    public void exportToTsv(
            GrammarSentenceFile sentenceFile,
            String grammarExplanation,
            String characterBreakdown,
            Path outputPath)
            throws IOException {
        try (PrintWriter writer =
                new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))) {
            writeHeaders(writer);
            writeExercises(writer, sentenceFile, grammarExplanation, characterBreakdown);
        }
    }

    /**
     * Export multiple sentence files to a single TSV.
     *
     * @param sentenceFiles list of parsed sentence files with their resolved content
     * @param outputPath where to write the TSV
     * @throws IOException if file cannot be written
     */
    public void exportMultipleToTsv(List<ResolvedSentenceFile> sentenceFiles, Path outputPath)
            throws IOException {
        try (PrintWriter writer =
                new PrintWriter(Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))) {
            writeHeaders(writer);
            for (ResolvedSentenceFile resolved : sentenceFiles) {
                writeExercises(
                        writer,
                        resolved.sentenceFile(),
                        resolved.grammarExplanation(),
                        resolved.characterBreakdown());
            }
        }
    }

    private void writeHeaders(PrintWriter writer) {
        writer.println("#separator:Tab");
        writer.println("#html:true");
        writer.println("#notetype:Grammar Sentence");
        writer.println(
                "#columns:SentenceCN\tSentencePinyin\tSentenceEN\tWord\tPinyin\tGrammarPoint\tGrammarExplanation\tCharacterBreakdown\tTags");
    }

    private void writeExercises(
            PrintWriter writer,
            GrammarSentenceFile sentenceFile,
            String grammarExplanation,
            String characterBreakdown) {
        for (GrammarSentenceExercise exercise : sentenceFile.exercises()) {
            String line =
                    String.join(
                            "\t",
                            escapeForTsv(exercise.sentenceCNAsHtml()),
                            escapeForTsv(stripHighlightMarkers(exercise.sentencePinyin())),
                            escapeForTsv(exercise.sentenceENAsHtml()),
                            escapeForTsv(sentenceFile.word()),
                            escapeForTsv(sentenceFile.pinyin()),
                            escapeForTsv(sentenceFile.grammarPoint()),
                            escapeForTsv(grammarExplanation),
                            escapeForTsv(characterBreakdown),
                            "" // Tags - empty for now
                            );
            writer.println(line);
        }
    }

    /** Strip ==highlight== markers from pinyin (since pinyin doesn't need HTML highlighting). */
    private String stripHighlightMarkers(String text) {
        return text.replaceAll("==([^=]+)==", "$1");
    }

    /** Escape a string value for safe inclusion in TSV format. */
    private String escapeForTsv(String value) {
        if (value == null) {
            return "";
        }
        // If the value contains tabs, newlines, or quotes, we need to quote it
        if (value.contains("\t")
                || value.contains("\n")
                || value.contains("\r")
                || value.contains("\"")) {
            // Escape quotes by doubling them, then wrap in quotes
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String readFileContent(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /** Record to hold a sentence file with its resolved content. */
    public record ResolvedSentenceFile(
            GrammarSentenceFile sentenceFile,
            String grammarExplanation,
            String characterBreakdown) {}
}
