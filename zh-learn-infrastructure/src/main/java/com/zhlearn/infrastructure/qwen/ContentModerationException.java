package com.zhlearn.infrastructure.qwen;

import com.zhlearn.domain.exception.UnrecoverableProviderException;

/**
 * Exception thrown when Qwen TTS rejects content due to moderation policies. This represents an
 * unrecoverable error that should be handled gracefully by skipping the problematic content rather
 * than crashing the application.
 *
 * <p>This is a checked exception that forces callers to explicitly handle content moderation
 * failures. The abstract provider framework catches this and skips the affected voice.
 */
public class ContentModerationException extends UnrecoverableProviderException {
    public ContentModerationException(String message) {
        super(message);
    }

    public ContentModerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
