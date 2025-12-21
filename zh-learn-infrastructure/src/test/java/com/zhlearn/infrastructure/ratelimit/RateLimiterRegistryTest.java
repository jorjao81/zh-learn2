package com.zhlearn.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimiterRegistryTest {

    @Test
    void shouldCreateNewRateLimiterWhenNotExists() {
        RateLimiterRegistry registry = new RateLimiterRegistry();

        ProviderRateLimiter limiter =
                registry.getOrCreate("test-provider", RateLimiterConfig.forQwen());

        assertThat(limiter).isNotNull();
        assertThat(registry.contains("test-provider")).isTrue();
        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnExistingRateLimiterWhenExists() {
        RateLimiterRegistry registry = new RateLimiterRegistry();

        ProviderRateLimiter first =
                registry.getOrCreate("test-provider", RateLimiterConfig.forQwen());
        ProviderRateLimiter second =
                registry.getOrCreate("test-provider", RateLimiterConfig.forTencent());

        // Should be the same instance
        assertThat(second).isSameAs(first);
        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    void shouldMaintainSeparateLimitersPerProvider() {
        RateLimiterRegistry registry = new RateLimiterRegistry();

        ProviderRateLimiter qwen = registry.getOrCreate("qwen", RateLimiterConfig.forQwen());
        ProviderRateLimiter tencent =
                registry.getOrCreate("tencent", RateLimiterConfig.forTencent());

        assertThat(qwen).isNotSameAs(tencent);
        assertThat(registry.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnNullForUnregisteredProvider() {
        RateLimiterRegistry registry = new RateLimiterRegistry();

        assertThat(registry.get("nonexistent")).isNull();
        assertThat(registry.contains("nonexistent")).isFalse();
    }

    @Test
    void shouldGetExistingRateLimiter() {
        RateLimiterRegistry registry = new RateLimiterRegistry();

        ProviderRateLimiter created =
                registry.getOrCreate("test-provider", RateLimiterConfig.forQwen());
        ProviderRateLimiter retrieved = registry.get("test-provider");

        assertThat(retrieved).isSameAs(created);
    }
}
