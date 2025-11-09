package com.zhlearn.domain.model;

import java.net.URI;
import java.util.Optional;

public record Image(
        URI sourceUrl,
        String thumbnailUrl,
        Optional<String> title,
        int width,
        int height,
        String contentType) {
    public Image {
        if (sourceUrl == null) {
            throw new IllegalArgumentException("Image source URL cannot be null");
        }
        if (thumbnailUrl == null || thumbnailUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image thumbnail URL cannot be null or empty");
        }
        if (title == null) {
            throw new IllegalArgumentException("Image title cannot be null (use Optional.empty())");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("Image width must be positive, got: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Image height must be positive, got: " + height);
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Image content type cannot be null or empty");
        }
    }
}
