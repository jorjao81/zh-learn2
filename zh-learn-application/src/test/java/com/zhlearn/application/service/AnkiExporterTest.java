package com.zhlearn.application.service;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.WordAnalysis;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AnkiExporterTest {

    private static final String MEDIA_PROPERTY = "zhlearn.anki.media.dir";

    private String originalMediaDir;

    @BeforeEach
    void captureOriginalProperty() {
        originalMediaDir = System.getProperty(MEDIA_PROPERTY);
    }

    @AfterEach
    void restoreOriginalProperty() {
        if (originalMediaDir == null) {
            System.clearProperty(MEDIA_PROPERTY);
        } else {
            System.setProperty(MEDIA_PROPERTY, originalMediaDir);
        }
    }

    @Test
    void exportCopiesPronunciationIntoConfiguredAnkiMedia(@TempDir Path tempDir) throws IOException {
        Path mediaDir = tempDir.resolve("collection.media");
        Files.createDirectories(mediaDir);
        Path sourceAudio = tempDir.resolve("source.mp3");
        Files.write(sourceAudio, new byte[] {1, 2, 3});

        WordAnalysis analysis = sampleAnalysis(sourceAudio);
        Path output = tempDir.resolve("output.tsv");

        System.setProperty(MEDIA_PROPERTY, mediaDir.toString());
        new AnkiExporter(new ExamplesHtmlFormatter(), new AnkiMediaLocator()).exportToFile(List.of(analysis), output);

        Path copied = mediaDir.resolve(sourceAudio.getFileName());
        assertThat(Files.exists(copied)).isTrue();
        assertThat(Files.readAllBytes(copied)).isEqualTo(Files.readAllBytes(sourceAudio));

        List<String> lines = Files.readAllLines(output);
        assertThat(lines).anyMatch(line -> line.contains("[sound:" + copied.getFileName() + "]"));
    }

    @Test
    void exportDoesNotDuplicateAudioAlreadyInMediaDir(@TempDir Path tempDir) throws IOException {
        Path mediaDir = tempDir.resolve("collection.media");
        Files.createDirectories(mediaDir);
        Path existingAudio = mediaDir.resolve("existing.mp3");
        Files.write(existingAudio, new byte[] {4, 5, 6});
        FileTime before = Files.getLastModifiedTime(existingAudio);

        WordAnalysis analysis = sampleAnalysis(existingAudio);
        Path output = tempDir.resolve("output.tsv");

        System.setProperty(MEDIA_PROPERTY, mediaDir.toString());
        new AnkiExporter(new ExamplesHtmlFormatter(), new AnkiMediaLocator()).exportToFile(List.of(analysis), output);

        FileTime after = Files.getLastModifiedTime(existingAudio);
        assertThat(after).isEqualTo(before);

        List<String> lines = Files.readAllLines(output);
        assertThat(lines).anyMatch(line -> line.contains("[sound:" + existingAudio.getFileName() + "]"));
    }

    private WordAnalysis sampleAnalysis(Path audioFile) {
        Hanzi hanzi = new Hanzi("学习");
        Pinyin pinyin = new Pinyin("xuéxí");
        Definition definition = new Definition("to study");
        StructuralDecomposition decomposition = new StructuralDecomposition("⺍ + 子");
        Example example = new Example(
            List.of(new Example.Usage("我学习中文", "wǒ xuéxí zhōngwén", "I study Chinese", "", "")),
            List.of()
        );
        Explanation explanation = new Explanation("Explanation");
        return new WordAnalysis(
            hanzi,
            pinyin,
            definition,
            decomposition,
            example,
            explanation,
            Optional.of(audioFile)
        );
    }
}
