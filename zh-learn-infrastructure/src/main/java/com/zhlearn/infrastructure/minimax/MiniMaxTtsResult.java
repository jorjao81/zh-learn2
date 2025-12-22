package com.zhlearn.infrastructure.minimax;

/**
 * Result from MiniMax TTS synthesis containing raw audio bytes and request metadata.
 *
 * @param audioData the synthesized audio as MP3 bytes
 * @param requestId the request ID from MiniMax API for debugging
 */
public record MiniMaxTtsResult(byte[] audioData, String requestId) {

    public MiniMaxTtsResult {
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("audioData must not be null or empty");
        }
    }
}
