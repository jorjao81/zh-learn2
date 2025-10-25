package com.zhlearn.application.image;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ImageCandidate;
import com.zhlearn.domain.provider.ImageProvider;
import com.zhlearn.infrastructure.image.ImageDownloader;

/** Orchestrates image search and parallel downloading for Chinese words. */
public class ImageOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(ImageOrchestrator.class);
    private static final int MAX_IMAGES = 10;

    private final ImageProvider imageProvider;
    private final ImageDownloader imageDownloader;
    private final ExecutorService executorService;

    public ImageOrchestrator(
            ImageProvider imageProvider,
            ImageDownloader imageDownloader,
            ExecutorService executorService) {
        this.imageProvider = imageProvider;
        this.imageDownloader = imageDownloader;
        this.executorService = executorService;
    }

    /**
     * Get image candidates for a word and definition by searching and downloading in parallel.
     *
     * @param word the Chinese word
     * @param definition the English definition
     * @return list of successfully downloaded image candidates
     * @throws IllegalStateException if no images could be downloaded
     */
    public List<ImageCandidate> getImageCandidates(Hanzi word, Definition definition) {
        log.info(
                "[Image] Starting image candidate generation for '{}' ({})",
                word.characters(),
                definition.meaning());

        // 1. Search for images
        List<Image> images = imageProvider.searchImages(word, definition, MAX_IMAGES);
        log.info(
                "[Image] Found {} images from provider '{}'",
                images.size(),
                imageProvider.getName());

        // 2. Download images in parallel
        List<CompletableFuture<ImageCandidate>> downloadFutures =
                images.stream()
                        .map(
                                image ->
                                        CompletableFuture.supplyAsync(
                                                () -> downloadImage(image), executorService))
                        .toList();

        // 3. Collect successful downloads (filter out failures)
        List<ImageCandidate> candidates =
                downloadFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(candidate -> candidate != null)
                        .collect(Collectors.toList());

        log.info(
                "[Image] Completed image downloads for '{}' - {} successful out of {}",
                word.characters(),
                candidates.size(),
                images.size());

        // 4. Fail-fast if no images downloaded successfully
        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                    "Failed to download any images for '"
                            + word.characters()
                            + "' ("
                            + definition.meaning()
                            + "). No images found after downloading candidates.");
        }

        return candidates;
    }

    private ImageCandidate downloadImage(Image image) {
        try {
            long startTime = System.currentTimeMillis();
            log.debug("[Image] Downloading image from: {}", image.sourceUrl());

            ImageCandidate candidate = imageDownloader.download(image);

            long duration = System.currentTimeMillis() - startTime;
            log.debug(
                    "[Image] Successfully downloaded image in {}ms: {}",
                    duration,
                    candidate.localPath().getFileName());

            return candidate;
        } catch (IllegalStateException e) {
            log.warn(
                    "[Image] Failed to download image from {}: {}",
                    image.sourceUrl(),
                    e.getMessage());
            return null; // Return null for failed downloads, will be filtered out
        }
    }
}
