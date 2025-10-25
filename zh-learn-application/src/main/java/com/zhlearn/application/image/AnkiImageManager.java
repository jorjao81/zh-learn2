package com.zhlearn.application.image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhlearn.application.audio.AnkiMediaLocator;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ImageCandidate;

/** Manages copying downloaded images to the Anki media directory. */
public class AnkiImageManager {
    private static final Logger log = LoggerFactory.getLogger(AnkiImageManager.class);

    private final AnkiMediaLocator mediaLocator;

    public AnkiImageManager(AnkiMediaLocator mediaLocator) {
        this.mediaLocator = mediaLocator;
    }

    /**
     * Copy image candidates to the Anki media directory with standardized filenames.
     *
     * @param candidates the list of downloaded image candidates
     * @param word the Chinese word (used in filename generation)
     * @return list of paths to the copied files in the Anki media directory
     * @throws IllegalStateException if Anki media directory cannot be found or files cannot be
     *     copied
     */
    public List<Path> copyImagesToAnkiMedia(List<ImageCandidate> candidates, Hanzi word) {
        log.info(
                "[AnkiImageManager] Copying {} images to Anki media for '{}'",
                candidates.size(),
                word.characters());

        // Find Anki media directory
        Optional<Path> mediaDirOpt = mediaLocator.locate();
        if (mediaDirOpt.isEmpty()) {
            throw new IllegalStateException(
                    "Anki media directory not found. "
                            + "Set ZHLEARN_ANKI_MEDIA_DIR or ANKI_MEDIA_DIR environment variable, "
                            + "or ensure Anki is installed in the default location.");
        }

        Path mediaDir = mediaDirOpt.get().toAbsolutePath().normalize();
        log.debug("[AnkiImageManager] Using Anki media directory: {}", mediaDir);

        try {
            // Ensure media directory exists
            Files.createDirectories(mediaDir);

            List<Path> copiedPaths = new ArrayList<>();

            // Copy each image with numbered filename
            for (int i = 0; i < candidates.size(); i++) {
                ImageCandidate candidate = candidates.get(i);
                String extension = getFileExtension(candidate.metadata().contentType());
                String filename = "zh-" + word.characters() + "-" + (i + 1) + extension;

                Path source = candidate.localPath().toAbsolutePath().normalize();
                Path target = mediaDir.resolve(filename);

                log.debug(
                        "[AnkiImageManager] Copying {} -> {}",
                        source.getFileName(),
                        target.getFileName());

                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                copiedPaths.add(target.toAbsolutePath());
            }

            log.info(
                    "[AnkiImageManager] Successfully copied {} images to Anki media",
                    copiedPaths.size());
            return copiedPaths;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to copy images to Anki media directory: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default ->
                    ".jpg"; // Default to .jpg for unknown types (should not happen with validation)
        };
    }
}
