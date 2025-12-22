package com.zhlearn.infrastructure.ratelimit;

import java.time.Duration;

/**
 * Configuration for per-provider rate limiters. Different providers may have different rate limits
 * and backoff strategies.
 *
 * @param maxBurst Maximum tokens (burst capacity)
 * @param baseRate Base refill rate in tokens per second
 * @param minRate Minimum rate during heavy backoff
 * @param backoffMultiplier Rate reduction on 429 (e.g., 0.5 = halve the rate)
 * @param recoveryMultiplier Rate increase on success (e.g., 1.1 = 10% increase)
 * @param defaultBackoff Default pause duration when no Retry-After header
 */
public record RateLimiterConfig(
        int maxBurst,
        double baseRate,
        double minRate,
        double backoffMultiplier,
        double recoveryMultiplier,
        Duration defaultBackoff) {

    /** Configuration for Qwen TTS - moderate burst, aggressive backoff. */
    public static RateLimiterConfig forQwen() {
        return new RateLimiterConfig(
                5, // 5 concurrent requests burst
                2.0, // 2 requests/second base rate
                0.1, // minimum 0.1 req/sec during heavy throttling
                0.5, // halve rate on 429
                1.1, // 10% recovery per success
                Duration.ofSeconds(10) // 10 second pause on 429
                );
    }

    /** Configuration for Tencent TTS - more permissive. */
    public static RateLimiterConfig forTencent() {
        return new RateLimiterConfig(
                10, // 10 concurrent requests burst
                5.0, // 5 requests/second base rate
                0.5, // minimum 0.5 req/sec
                0.5, // halve rate on 429
                1.2, // 20% recovery per success
                Duration.ofSeconds(5) // 5 second pause on 429
                );
    }

    /** Configuration for Forvo - strict rate limiting. */
    public static RateLimiterConfig forForvo() {
        return new RateLimiterConfig(
                2, // only 2 concurrent requests
                0.5, // 0.5 requests/second (1 every 2 seconds)
                0.1, // minimum rate
                0.3, // aggressive reduction on 429
                1.05, // slow recovery
                Duration.ofSeconds(30) // 30 second pause on 429
                );
    }
}
