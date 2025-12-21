package com.zhlearn.infrastructure.ratelimit;

import java.time.Duration;

/**
 * Per-provider rate limiter that coordinates requests across threads. Implementations should be
 * thread-safe and use adaptive throttling based on 429 responses.
 */
public interface ProviderRateLimiter {

    /**
     * Acquire permission to make a request. May block if rate limit is in effect.
     *
     * @param maxWait Maximum time to wait for permission
     * @return true if permission granted, false if timed out
     * @throws InterruptedException if thread is interrupted while waiting
     */
    boolean acquire(Duration maxWait) throws InterruptedException;

    /**
     * Notify the rate limiter that a request received HTTP 429. This triggers adaptive slowdown for
     * all pending requests to this provider.
     *
     * @param retryAfterHint Optional hint from Retry-After header (null if not present)
     */
    void notifyRateLimited(Duration retryAfterHint);

    /** Notify the rate limiter that a request succeeded. This allows gradual recovery of rate. */
    void notifySuccess();

    /**
     * Get the current effective rate limit (tokens per second). Useful for logging and monitoring.
     */
    double getCurrentRate();

    /** Check if currently in a backoff period where all requests are paused. */
    boolean isBackingOff();
}
