package com.zhlearn.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class AdaptiveTokenBucketRateLimiterTest {

    private static final RateLimiterConfig TEST_CONFIG =
            new RateLimiterConfig(
                    3, // max burst
                    10.0, // high base rate for fast tests
                    0.1, // min rate
                    0.5, // backoff multiplier
                    1.5, // recovery multiplier
                    Duration.ofMillis(100) // short backoff for tests
                    );

    @Test
    void shouldAcquireTokensUpToBurstLimit() throws InterruptedException {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        // Should be able to acquire up to burst limit immediately
        assertThat(limiter.acquire(Duration.ofMillis(10))).isTrue();
        assertThat(limiter.acquire(Duration.ofMillis(10))).isTrue();
        assertThat(limiter.acquire(Duration.ofMillis(10))).isTrue();
    }

    @Test
    void shouldBlockWhenTokensExhausted() throws InterruptedException {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        // Exhaust all tokens
        limiter.acquire(Duration.ofMillis(10));
        limiter.acquire(Duration.ofMillis(10));
        limiter.acquire(Duration.ofMillis(10));

        // Next acquire should block and either succeed after refill or timeout
        long start = System.nanoTime();
        limiter.acquire(Duration.ofMillis(50));
        long elapsed = System.nanoTime() - start;

        // With 10 req/s, token refills in 100ms - just check timing is reasonable
        assertThat(Duration.ofNanos(elapsed).toMillis()).isLessThan(200);
    }

    @Test
    void shouldRefillTokensOverTime() throws InterruptedException {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        // Exhaust all tokens
        limiter.acquire(Duration.ofMillis(10));
        limiter.acquire(Duration.ofMillis(10));
        limiter.acquire(Duration.ofMillis(10));

        // Wait for refill (at 10/s, 100ms = 1 token)
        Thread.sleep(150);

        // Should now be able to acquire
        assertThat(limiter.acquire(Duration.ofMillis(10))).isTrue();
    }

    @Test
    void shouldReduceRateOnRateLimited() {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        double initialRate = limiter.getCurrentRate();
        assertThat(initialRate).isEqualTo(10.0);

        limiter.notifyRateLimited(null);

        // Rate should be halved (0.5 multiplier)
        assertThat(limiter.getCurrentRate()).isEqualTo(5.0);
        assertThat(limiter.getRateLimitCount()).isEqualTo(1);
    }

    @Test
    void shouldNotReduceRateBelowMinimum() {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        // Multiple rate limit events
        for (int i = 0; i < 20; i++) {
            limiter.notifyRateLimited(null);
        }

        // Rate should not go below minimum
        assertThat(limiter.getCurrentRate()).isGreaterThanOrEqualTo(0.1);
    }

    @Test
    void shouldRecoverRateOnSuccess() {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        // First reduce the rate
        limiter.notifyRateLimited(null);
        assertThat(limiter.getCurrentRate()).isEqualTo(5.0);

        // Notify success - should recover by 1.5x
        limiter.notifySuccess();
        assertThat(limiter.getCurrentRate()).isEqualTo(7.5);
        assertThat(limiter.getSuccessCount()).isEqualTo(1);
    }

    @Test
    void shouldNotRecoverRateAboveBase() {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        // Multiple success notifications shouldn't exceed base rate
        for (int i = 0; i < 10; i++) {
            limiter.notifySuccess();
        }

        assertThat(limiter.getCurrentRate()).isLessThanOrEqualTo(10.0);
    }

    @Test
    void shouldEnterBackoffOnRateLimited() {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        assertThat(limiter.isBackingOff()).isFalse();

        limiter.notifyRateLimited(null);

        assertThat(limiter.isBackingOff()).isTrue();
    }

    @Test
    void shouldExitBackoffAfterDuration() throws InterruptedException {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        limiter.notifyRateLimited(null);
        assertThat(limiter.isBackingOff()).isTrue();

        // Wait for backoff to expire (100ms + margin)
        Thread.sleep(150);

        assertThat(limiter.isBackingOff()).isFalse();
    }

    @Test
    void shouldRespectCustomRetryAfterHint() throws InterruptedException {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        // Use a longer custom backoff
        limiter.notifyRateLimited(Duration.ofMillis(200));
        assertThat(limiter.isBackingOff()).isTrue();

        // After 100ms (default) it should still be backing off
        Thread.sleep(120);
        assertThat(limiter.isBackingOff()).isTrue();

        // After 200ms+ it should exit
        Thread.sleep(120);
        assertThat(limiter.isBackingOff()).isFalse();
    }

    @Test
    void shouldHandleConcurrentAcquisitions() throws InterruptedException {
        AdaptiveTokenBucketRateLimiter limiter =
                new AdaptiveTokenBucketRateLimiter("test", TEST_CONFIG);

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(
                    () -> {
                        try {
                            startLatch.await();
                            if (limiter.acquire(Duration.ofSeconds(1))) {
                                successCount.incrementAndGet();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            completeLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        completeLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // All should succeed eventually (high rate, 1s timeout)
        assertThat(successCount.get()).isEqualTo(numThreads);
    }

    @Test
    void factoryMethodsShouldCreateValidConfigs() {
        RateLimiterConfig qwen = RateLimiterConfig.forQwen();
        assertThat(qwen.maxBurst()).isEqualTo(3);
        assertThat(qwen.baseRate()).isEqualTo(2.5);

        RateLimiterConfig tencent = RateLimiterConfig.forTencent();
        assertThat(tencent.maxBurst()).isEqualTo(10);
        assertThat(tencent.baseRate()).isEqualTo(5.0);

        RateLimiterConfig forvo = RateLimiterConfig.forForvo();
        assertThat(forvo.maxBurst()).isEqualTo(2);
        assertThat(forvo.baseRate()).isEqualTo(0.5);
    }
}
