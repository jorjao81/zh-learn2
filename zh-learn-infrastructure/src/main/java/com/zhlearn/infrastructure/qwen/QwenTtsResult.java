package com.zhlearn.infrastructure.qwen;

import java.net.URI;

public record QwenTtsResult(URI audioUrl, String requestId) {
    public QwenTtsResult {
        if (audioUrl == null) {
            throw new IllegalArgumentException("audioUrl cannot be null");
        }
    }
}
