package com.zhlearn.application.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ImageCandidate;

class AnkiImageManagerTest {

    @TempDir Path tempDir;

    @Test
    void shouldCopyImagesWithSupportedMimeType() throws IOException {
        Path mediaDir = tempDir.resolve("anki-media");
        Files.createDirectories(mediaDir);

        AnkiImageManager manager = new AnkiImageManager(new StubAnkiMediaLocator(mediaDir));

        Path source = tempDir.resolve("source.jpg");
        Files.writeString(source, "fake-image-data");

        Image image =
                new Image(
                        URI.create("https://example.com/image.jpg"),
                        "https://example.com/thumb.jpg",
                        Optional.of("Title"),
                        800,
                        600,
                        "image/jpeg");

        ImageCandidate candidate = new ImageCandidate(image, source);

        List<Path> copied = manager.copyImagesToAnkiMedia(List.of(candidate), new Hanzi("学习"));

        Path expectedTarget = mediaDir.resolve("zh-学习-1.jpg").toAbsolutePath();
        assertThat(copied).containsExactly(expectedTarget);
        assertThat(Files.exists(expectedTarget)).isTrue();
    }

    @Test
    void shouldRejectUnsupportedMimeType() throws IOException {
        Path mediaDir = tempDir.resolve("anki-media");
        Files.createDirectories(mediaDir);

        AnkiImageManager manager = new AnkiImageManager(new StubAnkiMediaLocator(mediaDir));

        Path source = tempDir.resolve("source.bin");
        Files.writeString(source, "data");

        Image image =
                new Image(
                        URI.create("https://example.com/image.svg"),
                        "https://example.com/thumb.svg",
                        Optional.empty(),
                        320,
                        240,
                        "image/svg+xml");

        ImageCandidate candidate = new ImageCandidate(image, source);

        assertThatThrownBy(() -> manager.copyImagesToAnkiMedia(List.of(candidate), new Hanzi("象")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported image content type");
    }

    private static final class StubAnkiMediaLocator extends AnkiMediaLocator {
        private final Path directory;

        private StubAnkiMediaLocator(Path directory) {
            this.directory = directory;
        }

        @Override
        public Optional<Path> locate() {
            return Optional.of(directory);
        }
    }
}
