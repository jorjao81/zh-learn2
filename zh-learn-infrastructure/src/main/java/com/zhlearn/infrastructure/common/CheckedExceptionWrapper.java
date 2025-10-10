package com.zhlearn.infrastructure.common;

import java.io.IOException;

import com.zhlearn.domain.exception.UnrecoverableProviderException;

/**
 * Type-safe wrapper for checked exceptions in lambda/retry scenarios.
 *
 * <p>This wrapper allows checked exceptions to be thrown from lambdas (which cannot declare checked
 * exceptions) and then safely unwrapped. The wrapper accepts IOException, InterruptedException, and
 * UnrecoverableProviderException via its factory methods, providing compile-time safety.
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

    public static CheckedExceptionWrapper wrap(UnrecoverableProviderException e) {
        return new CheckedExceptionWrapper(e);
    }

    /**
     * Returns the wrapped checked exception for explicit re-throwing.
     *
     * <p>For InterruptedException, restores the thread's interrupted status before returning the
     * exception.
     *
     * @return the wrapped checked exception
     * @throws IOException if wrapped exception is IOException
     * @throws InterruptedException if wrapped exception is InterruptedException
     * @throws UnrecoverableProviderException if wrapped exception is UnrecoverableProviderException
     */
    public Throwable unwrap() throws IOException, InterruptedException, UnrecoverableProviderException {
        Throwable cause = getCause();
        if (cause instanceof IOException io) {
            return io;
        }
        if (cause instanceof InterruptedException ie) {
            Thread.currentThread().interrupt();
            return ie;
        }
        if (cause instanceof UnrecoverableProviderException upe) {
            return upe;
        }
        throw new AssertionError("Unexpected wrapped exception type: " + cause.getClass(), cause);
    }

    /**
     * Unwraps and re-throws the wrapped checked exception.
     *
     * <p>Provided for compatibility with callers that expect a re-throwing helper.
     *
     * @throws IOException if wrapped exception is IOException
     * @throws InterruptedException if wrapped exception is InterruptedException
     * @throws UnrecoverableProviderException if wrapped exception is UnrecoverableProviderException
     */
    public void unwrapAndThrow()
            throws IOException, InterruptedException, UnrecoverableProviderException {
        throw unwrap();
    }
}
