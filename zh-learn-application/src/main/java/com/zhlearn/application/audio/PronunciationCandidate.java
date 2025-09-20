package com.zhlearn.application.audio;

import java.nio.file.Path;

public record PronunciationCandidate(String label, Path file, String description) {

    public PronunciationCandidate(String label, Path file) {
        this(label, file, null);
    }

    public String displayDescription() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        return file.getFileName() != null ? file.getFileName().toString() : file.toString();
    }
}
