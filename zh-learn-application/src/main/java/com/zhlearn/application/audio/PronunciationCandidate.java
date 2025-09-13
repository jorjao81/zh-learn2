package com.zhlearn.application.audio;

import java.nio.file.Path;

public record PronunciationCandidate(String label, String soundNotation, Path file) {
}

