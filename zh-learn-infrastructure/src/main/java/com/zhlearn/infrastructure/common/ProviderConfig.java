package com.zhlearn.infrastructure.common;

import java.util.function.Function;

public record ProviderConfig<T>(
    String apiKey,
    String baseUrl,
    String modelName,
    Double temperature,
    Integer maxTokens,
    String templateResourcePath,
    String examplesResourcePath,
    Function<String, T> responseMapper,
    String providerName,
    String errorMessagePrefix
) {
}
