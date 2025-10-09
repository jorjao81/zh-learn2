package com.zhlearn.domain.exception;

/**
 * Checked exception for provider failures that are unrecoverable and should be handled gracefully.
 *
 * <p>Providers throwing this exception signal that the failure cannot be retried (e.g., content
 * moderation rejection) and the provider should be skipped. The orchestrator catches this exception
 * and continues processing with other providers.
 *
 * <p>This is a checked exception to force explicit handling and prevent it from being wrapped in
 * CompletionException during async operations.
 */
public abstract class UnrecoverableProviderException extends Exception {

    protected UnrecoverableProviderException(String message) {
        super(message);
    }

    protected UnrecoverableProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
