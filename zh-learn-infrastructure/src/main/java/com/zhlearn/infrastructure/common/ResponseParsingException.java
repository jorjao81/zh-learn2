package com.zhlearn.infrastructure.common;

/**
 * Exception thrown when parsing AI provider responses fails.
 *
 * <p>This exception represents failures in parsing YAML or JSON responses from AI providers. It is
 * a specific RuntimeException type that can be caught by provider implementations to add contextual
 * information (such as which word was being processed) before re-throwing.
 *
 * <p>This is used instead of generic RuntimeException to comply with linter rules that forbid
 * catching broad exception types.
 */
public class ResponseParsingException extends RuntimeException {}
