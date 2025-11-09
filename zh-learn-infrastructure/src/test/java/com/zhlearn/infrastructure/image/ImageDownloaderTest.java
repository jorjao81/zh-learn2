package com.zhlearn.infrastructure.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ImageCandidate;

class ImageDownloaderTest {

    @TempDir Path tempDir;

    @Test
    void shouldDownloadImageToManagedDirectory() throws Exception {
        byte[] payload = "fake-bytes".getBytes(StandardCharsets.UTF_8);

        HttpClient client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> response = mock(HttpResponse.class);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(payload);

        ImageDownloader downloader = new ImageDownloader(client, tempDir);

        Image image =
                new Image(
                        URI.create("https://example.com/image.jpg"),
                        "https://example.com/thumb.jpg",
                        Optional.empty(),
                        400,
                        300,
                        "image/jpeg");

        ImageCandidate candidate = downloader.download(image);

        assertThat(candidate.metadata()).isEqualTo(image);
        assertThat(candidate.localPath().getParent()).isEqualTo(tempDir.toAbsolutePath());
        assertThat(Files.readAllBytes(candidate.localPath())).isEqualTo(payload);
    }

    @Test
    void shouldRejectUnsupportedContentType() {
        HttpClient client = mock(HttpClient.class);
        ImageDownloader downloader = new ImageDownloader(client, tempDir);

        Image image =
                new Image(
                        URI.create("https://example.com/image.svg"),
                        "https://example.com/thumb.svg",
                        Optional.empty(),
                        400,
                        300,
                        "image/svg+xml");

        assertThatThrownBy(() -> downloader.download(image))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid content type");
    }
}
