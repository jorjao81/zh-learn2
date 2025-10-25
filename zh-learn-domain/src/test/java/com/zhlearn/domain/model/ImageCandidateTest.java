package com.zhlearn.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ImageCandidateTest {

    @Test
    void shouldCreateValidImageCandidate() {
        Image metadata =
                new Image(
                        URI.create("https://example.com/image.jpg"),
                        "https://example.com/thumb.jpg",
                        Optional.of("Test image"),
                        800,
                        600,
                        "image/jpeg");
        Path localPath = Paths.get("/tmp/image.jpg");

        ImageCandidate candidate = new ImageCandidate(metadata, localPath);

        assertThat(candidate.metadata()).isEqualTo(metadata);
        assertThat(candidate.localPath()).isEqualTo(localPath);
    }

    @Test
    void shouldThrowExceptionForNullMetadata() {
        Path localPath = Paths.get("/tmp/image.jpg");

        assertThatThrownBy(() -> new ImageCandidate(null, localPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image metadata cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullLocalPath() {
        Image metadata =
                new Image(
                        URI.create("https://example.com/image.jpg"),
                        "https://example.com/thumb.jpg",
                        Optional.of("Test image"),
                        800,
                        600,
                        "image/jpeg");

        assertThatThrownBy(() -> new ImageCandidate(metadata, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Local path cannot be null");
    }
}
