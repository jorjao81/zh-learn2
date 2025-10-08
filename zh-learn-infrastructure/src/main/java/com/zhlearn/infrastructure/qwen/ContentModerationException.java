package com.zhlearn.infrastructure.qwen;

import com.zhlearn.domain.exception.GracefulProviderFailureException;

/**
 * Exception thrown when Qwen TTS rejects content due to moderation policies. This represents an
 * unrecoverable error that should be handled gracefully by skipping the problematic content rather
 * than crashing the application.
 *
 * <p>This is a RuntimeException to avoid polluting method signatures while still being catchable by
 * the abstract provider framework via isSkippableException().
 */
public class ContentModerationException extends RuntimeException
        implements GracefulProviderFailureException {
    public ContentModerationException(String message) {
        super(message);
    }

    public ContentModerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
