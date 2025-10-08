package com.zhlearn.infrastructure.tencent;

public record TencentTtsResult(String audioData, String sessionId) {
    public TencentTtsResult {
        if (audioData == null || audioData.isBlank()) {
            throw new IllegalArgumentException("audioData cannot be null or blank");
        }
    }
}
