package com.zhlearn.infrastructure.common;

import java.io.IOException;

/**
 * Type-safe wrapper for checked exceptions in lambda/retry scenarios.
 *
 * <p>This wrapper allows checked exceptions to be thrown from lambdas (which cannot declare checked
 * exceptions) and then safely unwrapped. The wrapper only accepts IOException and
 * InterruptedException via its factory methods, providing compile-time safety.
 *
 * <p>Usage:
 *
 * <pre>
 * try {
 *     return retry.invoke(() -> {
 *         try {
 *             return methodThatThrowsChecked();
 *         } catch (IOException | InterruptedException e) {
 *             throw CheckedExceptionWrapper.wrap(e);
 *         }
 *     });
 * } catch (CheckedExceptionWrapper wrapper) {
 *     wrapper.unwrapAndThrow();
 * }
 * </pre>
 */
public final class CheckedExceptionWrapper extends RuntimeException {

    private CheckedExceptionWrapper(Throwable cause) {
        super(cause);
    }

    public static CheckedExceptionWrapper wrap(IOException e) {
        return new CheckedExceptionWrapper(e);
    }

    public static CheckedExceptionWrapper wrap(InterruptedException e) {
        return new CheckedExceptionWrapper(e);
    }

    /**
     * Unwraps and re-throws the wrapped checked exception.
     *
     * <p>For InterruptedException, also restores the thread's interrupted status before throwing.
     *
     * @throws IOException if wrapped exception is IOException
     * @throws InterruptedException if wrapped exception is InterruptedException
     */
    public void unwrapAndThrow() throws IOException, InterruptedException {
        Throwable cause = getCause();
        if (cause instanceof IOException io) {
            throw io;
        }
        if (cause instanceof InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw ie;
        }
        throw new AssertionError("Unexpected wrapped exception type: " + cause.getClass(), cause);
    }
}
