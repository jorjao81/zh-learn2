package com.zhlearn.infrastructure.ratelimit;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptive token bucket rate limiter with backpressure support. Thread-safe, designed for
 * high-concurrency use across multiple parallel requests to the same provider.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Token bucket algorithm for burst tolerance
 *   <li>Adaptive rate reduction on 429 responses
 *   <li>Global pause that affects ALL waiting requests
 *   <li>Gradual rate recovery on successful requests
 * </ul>
 */
public final class AdaptiveTokenBucketRateLimiter implements ProviderRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(AdaptiveTokenBucketRateLimiter.class);

    private final String providerName;
    private final int maxTokens;
    private final double baseRefillRate;
    private final double minRefillRate;
    private final double backoffMultiplier;
    private final double recoveryMultiplier;
    private final Duration defaultBackoffDuration;

    private final AtomicInteger tokens;
    private final AtomicLong lastRefillNanos;
    private final AtomicLong pauseUntilNanos;

    // Use volatile for rate since it's read frequently, written occasionally
    private volatile double currentRefillRate;

    // Lock for coordinated waiting
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition tokensAvailable = lock.newCondition();

    // Statistics for monitoring
    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong rateLimitCount = new AtomicLong();

    /**
     * Create a rate limiter from configuration.
     *
     * @param providerName Name for logging
     * @param config Rate limiter configuration
     */
    public AdaptiveTokenBucketRateLimiter(String providerName, RateLimiterConfig config) {
        this.providerName = providerName;
        this.maxTokens = config.maxBurst();
        this.baseRefillRate = config.baseRate();
        this.minRefillRate = config.minRate();
        this.backoffMultiplier = config.backoffMultiplier();
        this.recoveryMultiplier = config.recoveryMultiplier();
        this.defaultBackoffDuration = config.defaultBackoff();

        this.tokens = new AtomicInteger(maxTokens);
        this.lastRefillNanos = new AtomicLong(System.nanoTime());
        this.pauseUntilNanos = new AtomicLong(0);
        this.currentRefillRate = baseRefillRate;
    }

    @Override
    public boolean acquire(Duration maxWait) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + maxWait.toNanos();

        lock.lock();
        try {
            while (true) {
                // First check global pause
                long now = System.nanoTime();
                long pauseUntil = pauseUntilNanos.get();
                if (pauseUntil > now) {
                    long waitNanos = Math.min(pauseUntil - now, deadlineNanos - now);
                    if (waitNanos <= 0) {
                        log.debug(
                                "[{}] Rate limit acquire timed out during global pause",
                                providerName);
                        return false;
                    }
                    log.debug(
                            "[{}] Waiting {}ms for global pause to end",
                            providerName,
                            Duration.ofNanos(waitNanos).toMillis());
                    tokensAvailable.awaitNanos(waitNanos);
                    continue;
                }

                // Refill tokens based on elapsed time
                refillTokens();

                // Try to acquire a token
                int currentTokens = tokens.get();
                if (currentTokens > 0 && tokens.compareAndSet(currentTokens, currentTokens - 1)) {
                    log.trace("[{}] Acquired token, {} remaining", providerName, currentTokens - 1);
                    return true;
                }

                // No tokens available, calculate wait time
                long remainingNanos = deadlineNanos - System.nanoTime();
                if (remainingNanos <= 0) {
                    log.debug("[{}] Rate limit acquire timed out waiting for tokens", providerName);
                    return false;
                }

                // Wait for next token refill
                long waitForTokenNanos = (long) (1_000_000_000.0 / currentRefillRate);
                long actualWait = Math.min(waitForTokenNanos, remainingNanos);

                log.trace(
                        "[{}] No tokens, waiting {}ms for refill",
                        providerName,
                        Duration.ofNanos(actualWait).toMillis());
                tokensAvailable.awaitNanos(actualWait);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void notifyRateLimited(Duration retryAfterHint) {
        lock.lock();
        try {
            rateLimitCount.incrementAndGet();

            // Reduce refill rate
            double oldRate = currentRefillRate;
            double newRate = Math.max(currentRefillRate * backoffMultiplier, minRefillRate);
            currentRefillRate = newRate;

            // Set global pause - all waiting threads will see this
            Duration pauseDuration =
                    retryAfterHint != null ? retryAfterHint : defaultBackoffDuration;
            long newPauseUntil = System.nanoTime() + pauseDuration.toNanos();
            pauseUntilNanos.updateAndGet(current -> Math.max(current, newPauseUntil));

            log.warn(
                    "[{}] Rate limited! Reducing rate from {:.2f}/s to {:.2f}/s, pausing for {}ms",
                    providerName,
                    oldRate,
                    newRate,
                    pauseDuration.toMillis());

            // Wake up all waiting threads so they see the pause
            tokensAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void notifySuccess() {
        successCount.incrementAndGet();

        // Gradually recover rate (no lock needed for this pattern)
        double currentRate = currentRefillRate;
        if (currentRate < baseRefillRate) {
            double newRate = Math.min(currentRate * recoveryMultiplier, baseRefillRate);
            currentRefillRate = newRate;
            log.trace("[{}] Success - recovering rate to {:.2f}/s", providerName, newRate);
        }
    }

    @Override
    public double getCurrentRate() {
        return currentRefillRate;
    }

    @Override
    public boolean isBackingOff() {
        return pauseUntilNanos.get() > System.nanoTime();
    }

    /**
     * Get the number of successful requests since creation.
     *
     * @return success count
     */
    public long getSuccessCount() {
        return successCount.get();
    }

    /**
     * Get the number of rate limit events since creation.
     *
     * @return rate limit count
     */
    public long getRateLimitCount() {
        return rateLimitCount.get();
    }

    private void refillTokens() {
        long now = System.nanoTime();
        long lastRefill = lastRefillNanos.get();
        double elapsedSeconds = (now - lastRefill) / 1_000_000_000.0;

        int tokensToAdd = (int) (elapsedSeconds * currentRefillRate);
        if (tokensToAdd > 0 && lastRefillNanos.compareAndSet(lastRefill, now)) {
            int currentTokens = tokens.get();
            int newTokens = Math.min(currentTokens + tokensToAdd, maxTokens);
            tokens.set(newTokens);

            // Signal waiting threads that tokens are available
            tokensAvailable.signalAll();
        }
    }
}
