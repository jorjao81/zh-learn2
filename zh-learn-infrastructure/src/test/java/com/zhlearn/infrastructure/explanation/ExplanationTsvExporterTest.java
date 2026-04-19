package com.zhlearn.infrastructure.explanation;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zhlearn.domain.model.ExplanationEntry;

class ExplanationTsvExporterTest {

    private final ExplanationTsvExporter exporter = new ExplanationTsvExporter();

    @TempDir Path tempDir;

    @Test
    void shouldWriteHeaders() throws IOException {
        Path output = tempDir.resolve("test.tsv");
        exporter.exportToTsv(
                List.of(new ExplanationEntry("水域", "shuǐ yù", "## Meaning\ncontent", "word")),
                output,
                tempDir,
                null);

        List<String> lines = Files.readAllLines(output);
        assertThat(lines.get(0)).isEqualTo("#separator:Tab");
        assertThat(lines.get(1)).isEqualTo("#html:true");
        assertThat(lines.get(2)).isEqualTo("#notetype:Chinese Explanation");
        assertThat(lines.get(3))
                .startsWith("#columns:Simplified\tPinyin\tAudio\tExplanation\tEntryType\tTags");
    }

    @Test
    void shouldWriteOneRowPerEntry() throws IOException {
        Path output = tempDir.resolve("test.tsv");
        var entries =
                List.of(
                        new ExplanationEntry("水域", "shuǐ yù", "content one", "word"),
                        new ExplanationEntry("受益匪浅", "shòu yì fěi qiǎn", "content two", "idiom"));

        exporter.exportToTsv(entries, output, tempDir, null);

        // Single-line markdown content: 4 header lines + 2 data rows
        List<String> lines = Files.readAllLines(output);
        assertThat(lines).hasSize(6);
    }

    @Test
    void shouldIncludeEntryTypeAsTags() throws IOException {
        Path output = tempDir.resolve("test.tsv");
        exporter.exportToTsv(
                List.of(new ExplanationEntry("水域", "shuǐ yù", "content", "word")),
                output,
                tempDir,
                null);

        List<String> lines = Files.readAllLines(output);
        String dataLine = lines.get(4);
        String[] columns = dataLine.split("\t");
        assertThat(columns).hasSize(6);
        assertThat(columns[0]).isEqualTo("水域"); // Simplified
        assertThat(columns[2]).isEmpty(); // Audio (no audio)
        assertThat(columns[4]).isEqualTo("word"); // EntryType
        assertThat(columns[5]).isEqualTo("word"); // Tags
    }

    @Test
    void shouldStoreRawMarkdown() throws IOException {
        Path output = tempDir.resolve("test.tsv");
        exporter.exportToTsv(
                List.of(new ExplanationEntry("水域", "shuǐ yù", "**bold text**", "word")),
                output,
                tempDir,
                null);

        String content = Files.readString(output);
        // Markdown is stored raw, not converted to HTML
        assertThat(content).contains("**bold text**");
        assertThat(content).doesNotContain("<strong>");
    }

    @Test
    void shouldEscapeTabsInValues() throws IOException {
        Path output = tempDir.resolve("test.tsv");
        exporter.exportToTsv(
                List.of(new ExplanationEntry("水域", "shuǐ yù", "has\ttab", "word")),
                output,
                tempDir,
                null);

        String content = Files.readString(output);
        assertThat(content).contains("\"");
    }

    @Test
    void shouldIncludeAudioReference() throws IOException {
        // Create a fake audio file
        Path audioDir = tempDir.resolve("audio");
        Files.createDirectories(audioDir);
        Path audioFile = audioDir.resolve("test_水域.mp3");
        Files.write(audioFile, new byte[] {1, 2, 3});

        Path output = tempDir.resolve("test.tsv");
        exporter.exportToTsv(
                List.of(
                        new ExplanationEntry(
                                "水域", "shuǐ yù", "content", "word", "audio/test_水域.mp3")),
                output,
                tempDir,
                null);

        String content = Files.readString(output);
        assertThat(content).contains("[sound:test_水域.mp3]");
    }

    @Test
    void shouldCopyAudioToAnkiMediaDir() throws IOException {
        // Create a fake audio file
        Path audioDir = tempDir.resolve("audio");
        Files.createDirectories(audioDir);
        Path audioFile = audioDir.resolve("test_水域.mp3");
        Files.write(audioFile, new byte[] {1, 2, 3});

        Path ankiMedia = tempDir.resolve("anki-media");
        Files.createDirectories(ankiMedia);

        Path output = tempDir.resolve("test.tsv");
        exporter.exportToTsv(
                List.of(
                        new ExplanationEntry(
                                "水域", "shuǐ yù", "content", "word", "audio/test_水域.mp3")),
                output,
                tempDir,
                ankiMedia);

        assertThat(ankiMedia.resolve("test_水域.mp3")).exists();
        assertThat(Files.readAllBytes(ankiMedia.resolve("test_水域.mp3")))
                .isEqualTo(new byte[] {1, 2, 3});
    }
}
