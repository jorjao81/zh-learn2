package com.zhlearn.domain.model;

import java.nio.file.Path;

public record ImageCandidate(Image metadata, Path localPath) {
    public ImageCandidate {
        if (metadata == null) {
            throw new IllegalArgumentException("Image metadata cannot be null");
        }
        if (localPath == null) {
            throw new IllegalArgumentException("Local path cannot be null");
        }
    }
}
