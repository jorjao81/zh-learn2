package com.zhlearn.domain.exception;

/**
 * Marker interface for exceptions that represent graceful provider failures. These failures should
 * be handled by providers returning empty results rather than crashing the application.
 *
 * <p>Examples include content moderation failures, provider-specific rate limits that cannot be
 * resolved by retrying, etc.
 */
public interface GracefulProviderFailureException {}
