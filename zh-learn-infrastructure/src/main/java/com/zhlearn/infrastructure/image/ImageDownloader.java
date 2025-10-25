package com.zhlearn.infrastructure.image;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ImageCandidate;

/** Downloads images from URLs to temporary local files. */
public class ImageDownloader {
    private static final Logger log = LoggerFactory.getLogger(ImageDownloader.class);

    private static final Set<String> VALID_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final HttpClient httpClient;
    private final Path tempDirectory;

    public ImageDownloader() {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                createTempDirectory());
    }

    public ImageDownloader(HttpClient httpClient, Path tempDirectory) {
        this.httpClient = httpClient;
        this.tempDirectory = tempDirectory;
    }

    /**
     * Download an image from its source URL to a temporary local file.
     *
     * @param image the image metadata including source URL and content type
     * @return ImageCandidate with the image metadata and local file path
     * @throws IllegalStateException if download fails or content type is invalid
     */
    public ImageCandidate download(Image image) {
        URI sourceUrl = image.sourceUrl();
        String contentType = image.contentType();

        log.debug("[ImageDownloader] Downloading image from: {}", sourceUrl);

        // Validate content type
        if (!VALID_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalStateException(
                    "Invalid content type '"
                            + contentType
                            + "' for image from "
                            + sourceUrl
                            + ". Supported types: "
                            + VALID_CONTENT_TYPES);
        }

        try {
            // Download image
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(sourceUrl)
                            .timeout(Duration.ofSeconds(30))
                            .GET()
                            .build();

            HttpResponse<byte[]> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Failed to download image from "
                                + sourceUrl
                                + ": HTTP "
                                + response.statusCode());
            }

            // Generate unique filename based on URL hash
            String filename = generateFilename(sourceUrl, contentType);
            Path localPath = tempDirectory.resolve(filename);

            // Save to temp file
            Files.write(localPath, response.body());

            log.debug(
                    "[ImageDownloader] Successfully downloaded image to: {}",
                    localPath.toAbsolutePath());

            return new ImageCandidate(image, localPath.toAbsolutePath());

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to download image from " + sourceUrl + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Image download interrupted from " + sourceUrl + ": " + e.getMessage(), e);
        }
    }

    private String generateFilename(URI sourceUrl, String contentType) {
        // Hash the URL to create a unique but deterministic filename
        String urlString = sourceUrl.toString();
        String hash = hashUrl(urlString);

        // Determine file extension from content type
        String extension =
                switch (contentType) {
                    case "image/jpeg" -> ".jpg";
                    case "image/png" -> ".png";
                    case "image/webp" -> ".webp";
                    default -> ".bin";
                };

        return "img-" + hash + extension;
    }

    private String hashUrl(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(url.getBytes());
            String fullHash = HexFormat.of().formatHex(hashBytes);
            // Use first 16 characters for reasonable filename length
            return fullHash.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always be available
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private static Path createTempDirectory() {
        try {
            Path tempDir = Files.createTempDirectory("zh-learn-images-");
            log.debug("[ImageDownloader] Created temp directory: {}", tempDir.toAbsolutePath());
            return tempDir;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to create temporary directory for images: " + e.getMessage(), e);
        }
    }
}
