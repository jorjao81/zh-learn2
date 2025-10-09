package com.zhlearn.domain.exception;

/**
 * Base runtime exception for provider failures that should be handled gracefully by higher layers.
 *
 * <p>Providers throwing this exception signal that the failure is expected (e.g. content
 * moderation) and should not crash the application. The orchestrator can catch this exception and
 * skip the provider results.
 */
public abstract class GracefulProviderFailureException extends RuntimeException {

    protected GracefulProviderFailureException(String message) {
        super(message);
    }

    protected GracefulProviderFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
