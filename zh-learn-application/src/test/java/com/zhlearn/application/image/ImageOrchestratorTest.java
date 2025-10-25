package com.zhlearn.application.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ImageCandidate;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ImageProvider;
import com.zhlearn.infrastructure.image.ImageDownloader;

class ImageOrchestratorTest {

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void shouldReturnDownloadedImageCandidates() throws Exception {
        Image image1 = createImage("https://example.com/1.jpg", "https://example.com/thumb1.jpg");
        Image image2 = createImage("https://example.com/2.jpg", "https://example.com/thumb2.jpg");
        StubImageProvider provider = new StubImageProvider(List.of(image1, image2));

        Path path1 = Files.createTempFile("image-orch-1", ".jpg");
        Path path2 = Files.createTempFile("image-orch-2", ".jpg");
        StubImageDownloader downloader =
                new StubImageDownloader(
                        List.of(
                                new ImageCandidate(image1, path1),
                                new ImageCandidate(image2, path2)));

        ImageOrchestrator orchestrator =
                new ImageOrchestrator(provider, downloader, executorService);

        List<ImageCandidate> candidates =
                orchestrator.getImageCandidates(new Hanzi("学"), new Definition("study"));

        assertThat(candidates).hasSize(2);
        assertThat(new ArrayList<>(candidates))
                .extracting(ImageCandidate::localPath)
                .containsExactlyInAnyOrder(path1.toAbsolutePath(), path2.toAbsolutePath());
    }

    @Test
    void shouldFailWhenAllDownloadsFail() {
        Image image = createImage("https://example.com/only.jpg", "https://example.com/thumb.jpg");
        StubImageProvider provider = new StubImageProvider(List.of(image));
        AlwaysFailingDownloader downloader = new AlwaysFailingDownloader();

        ImageOrchestrator orchestrator =
                new ImageOrchestrator(provider, downloader, executorService);

        assertThatThrownBy(
                        () ->
                                orchestrator.getImageCandidates(
                                        new Hanzi("学"), new Definition("study")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to download any images");
    }

    private Image createImage(String url, String thumb) {
        return new Image(URI.create(url), thumb, Optional.of("title"), 800, 600, "image/jpeg");
    }

    private static final class StubImageProvider implements ImageProvider {
        private final List<Image> images;

        private StubImageProvider(List<Image> images) {
            this.images = images;
        }

        @Override
        public String getName() {
            return "stub";
        }

        @Override
        public String getDescription() {
            return "stub";
        }

        @Override
        public ProviderType getType() {
            return ProviderType.DUMMY;
        }

        @Override
        public List<Image> searchImages(Hanzi word, Definition definition, int maxResults) {
            return images;
        }
    }

    private static final class StubImageDownloader extends ImageDownloader {
        private final Deque<ImageCandidate> queue = new ArrayDeque<>();

        private StubImageDownloader(List<ImageCandidate> candidates) {
            super(
                    HttpClient.newBuilder().connectTimeout(Duration.ofMillis(1)).build(),
                    Path.of(System.getProperty("java.io.tmpdir")));
            queue.addAll(candidates);
        }

        @Override
        public ImageCandidate download(Image image) {
            if (queue.isEmpty()) {
                throw new IllegalStateException("No candidate for " + image.sourceUrl());
            }
            return queue.removeFirst();
        }
    }

    private static final class AlwaysFailingDownloader extends ImageDownloader {
        private AlwaysFailingDownloader() {
            super(
                    HttpClient.newBuilder().connectTimeout(Duration.ofMillis(1)).build(),
                    Path.of(System.getProperty("java.io.tmpdir")));
        }

        @Override
        public ImageCandidate download(Image image) {
            throw new IllegalStateException("Download failed");
        }
    }
}
