package com.zhlearn.infrastructure.grammar;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zhlearn.domain.model.GrammarSentenceExercise;
import com.zhlearn.domain.model.GrammarSentenceFile;

class GrammarSentenceTsvExporterTest {

    @TempDir Path tempDir;

    private GrammarSentenceFile createSampleFile() {
        return new GrammarSentenceFile(
                "似乎",
                "sìhū",
                "四11",
                "../HSK4-11-似乎.md",
                "../characters/似乎.md",
                List.of(
                        new GrammarSentenceExercise(
                                "天==似乎==要下雨了。",
                                "Tiān ==sìhū== yào xiàyǔ le.",
                                "It ==seems like== it's going to rain."),
                        new GrammarSentenceExercise(
                                "他==似乎==不太想去。",
                                "Tā ==sìhū== bù tài xiǎng qù.",
                                "He doesn't ==seem== to want to go.")));
    }

    @Test
    void shouldExportToTsvWithHeaders() throws IOException {
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        exporter.exportToTsv(
                createSampleFile(),
                "# Grammar Explanation\n\nSome explanation here.",
                "# Character Breakdown\n\nCharacter info here.",
                output);

        String content = Files.readString(output);
        assertThat(content).startsWith("#separator:Tab\n");
        assertThat(content).contains("#html:true\n");
        assertThat(content).contains("#notetype:Grammar Sentence\n");
        assertThat(content).contains("#columns:SentenceCN\t");
    }

    @Test
    void shouldConvertHighlightsToHtmlSpans() throws IOException {
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        exporter.exportToTsv(createSampleFile(), "# Grammar", "# Characters", output);

        String content = Files.readString(output);
        // Chinese should have HTML span (quotes are doubled in TSV format)
        assertThat(content).contains("<span class=\"\"hl\"\">似乎</span>");
        // English should have HTML span
        assertThat(content).contains("<span class=\"\"hl\"\">seems like</span>");
    }

    @Test
    void shouldStripHighlightMarkersFromPinyin() throws IOException {
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        exporter.exportToTsv(createSampleFile(), "# Grammar", "# Characters", output);

        String content = Files.readString(output);
        // Pinyin should NOT have highlight markers or HTML spans
        assertThat(content).contains("Tiān sìhū yào xiàyǔ le.");
        assertThat(content).doesNotContain("Tiān ==sìhū==");
        assertThat(content).doesNotContain("Tiān <span");
    }

    @Test
    void shouldIncludeWordAndPinyinInEachRow() throws IOException {
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        exporter.exportToTsv(createSampleFile(), "# Grammar", "# Characters", output);

        String content = Files.readString(output);
        String[] lines = content.split("\n");

        // Skip header lines (4 total), check data rows
        int dataRowCount = 0;
        for (String line : lines) {
            if (!line.startsWith("#")) {
                assertThat(line).contains("似乎");
                assertThat(line).contains("sìhū");
                assertThat(line).contains("四11");
                dataRowCount++;
            }
        }
        assertThat(dataRowCount).isEqualTo(2);
    }

    @Test
    void shouldEscapeQuotesInContent() throws IOException {
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        String grammarWithQuotes = "He said \"hello\" to me.\nNew line here.";
        exporter.exportToTsv(createSampleFile(), grammarWithQuotes, "", output);

        String content = Files.readString(output);
        // Quotes should be doubled and the field should be quoted
        assertThat(content).contains("\"\"hello\"\"");
    }

    @Test
    void shouldIncludeGrammarExplanationInEachRow() throws IOException {
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        String grammarExplanation = "# HSK 4 Grammar: 似乎\n\nThis is the explanation.";
        exporter.exportToTsv(createSampleFile(), grammarExplanation, "", output);

        String content = Files.readString(output);
        // Grammar explanation should appear in each data row
        assertThat(content).contains("HSK 4 Grammar");
    }

    @Test
    void shouldHandleEmptyCharacterBreakdown() throws IOException {
        GrammarSentenceFile fileWithoutChars =
                new GrammarSentenceFile(
                        "似乎",
                        "sìhū",
                        "四11",
                        "../HSK4-11-似乎.md",
                        null,
                        List.of(
                                new GrammarSentenceExercise(
                                        "天==似乎==要下雨了。",
                                        "Tiān sìhū yào xiàyǔ le.",
                                        "It ==seems like== it's going to rain.")));

        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        exporter.exportToTsv(fileWithoutChars, "# Grammar", "", output);

        String content = Files.readString(output);
        assertThat(content).isNotBlank();
        // Should have empty character breakdown field (tab followed by empty Tags)
        assertThat(content).contains("\t\t"); // Empty CharacterBreakdown followed by empty Tags
    }

    @Test
    void shouldExportMultipleFilesToSingleTsv() throws IOException {
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();
        Path output = tempDir.resolve("output.tsv");

        GrammarSentenceFile file1 =
                new GrammarSentenceFile(
                        "似乎",
                        "sìhū",
                        "四11",
                        "../HSK4-11-似乎.md",
                        null,
                        List.of(
                                new GrammarSentenceExercise(
                                        "天==似乎==要下雨了。",
                                        "Tiān sìhū yào xiàyǔ le.",
                                        "It ==seems like== it's going to rain.")));

        GrammarSentenceFile file2 =
                new GrammarSentenceFile(
                        "以便",
                        "yǐbiàn",
                        "五23",
                        "../HSK5-23-以便.md",
                        null,
                        List.of(
                                new GrammarSentenceExercise(
                                        "请提前通知我，==以便==我做好准备。",
                                        "Qǐng tíqián tōngzhī wǒ, yǐbiàn wǒ zuò hǎo zhǔnbèi.",
                                        "Please notify me in advance ==so that== I can be prepared.")));

        List<GrammarSentenceTsvExporter.ResolvedSentenceFile> files =
                List.of(
                        new GrammarSentenceTsvExporter.ResolvedSentenceFile(
                                file1, "# Grammar 1", ""),
                        new GrammarSentenceTsvExporter.ResolvedSentenceFile(
                                file2, "# Grammar 2", ""));

        exporter.exportMultipleToTsv(files, output);

        String content = Files.readString(output);
        assertThat(content).contains("似乎");
        assertThat(content).contains("以便");
        assertThat(content).contains("四11");
        assertThat(content).contains("五23");
    }
}
