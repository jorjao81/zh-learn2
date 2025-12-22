package com.zhlearn.infrastructure.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for per-provider rate limiters. Ensures each provider has exactly one rate limiter
 * instance, enabling coordination across all requests to that provider.
 */
public final class RateLimiterRegistry {

    private final Map<String, ProviderRateLimiter> limiters = new ConcurrentHashMap<>();

    /**
     * Get or create a rate limiter for the given provider. If a limiter already exists for this
     * provider, returns the existing one.
     *
     * @param providerName Unique provider identifier
     * @param config Configuration for the rate limiter
     * @return The rate limiter for this provider
     */
    public ProviderRateLimiter getOrCreate(String providerName, RateLimiterConfig config) {
        return limiters.computeIfAbsent(
                providerName, name -> new AdaptiveTokenBucketRateLimiter(name, config));
    }

    /**
     * Get an existing rate limiter, or null if not registered.
     *
     * @param providerName Provider name to look up
     * @return The rate limiter, or null
     */
    public ProviderRateLimiter get(String providerName) {
        return limiters.get(providerName);
    }

    /**
     * Check if a rate limiter exists for the given provider.
     *
     * @param providerName Provider name to check
     * @return true if registered
     */
    public boolean contains(String providerName) {
        return limiters.containsKey(providerName);
    }

    /**
     * Get the number of registered rate limiters.
     *
     * @return count
     */
    public int size() {
        return limiters.size();
    }
}
