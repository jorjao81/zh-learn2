package com.zhlearn.infrastructure.qwen;

import com.zhlearn.domain.exception.GracefulProviderFailureException;

/**
 * Exception thrown when Qwen TTS rejects content due to moderation policies. This represents an
 * unrecoverable error that should be handled gracefully by skipping the problematic content rather
 * than crashing the application.
 */
public class ContentModerationException extends Exception
        implements GracefulProviderFailureException {
    public ContentModerationException(String message) {
        super(message);
    }

    public ContentModerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
