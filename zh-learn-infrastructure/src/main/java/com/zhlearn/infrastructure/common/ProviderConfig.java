package com.zhlearn.infrastructure.common;

import java.util.function.Function;

public class ProviderConfig<T> {
    private final String apiKey;
    private final String baseUrl;
    private final String modelName;
    private final Double temperature;
    private final Integer maxTokens;
    private final String templateResourcePath;
    private final String examplesResourcePath;
    private final Function<String, T> responseMapper;
    private final String providerName;
    private final String errorMessagePrefix;

    public ProviderConfig(String apiKey, String baseUrl, String modelName, 
                         Double temperature, Integer maxTokens,
                         String templateResourcePath, String examplesResourcePath,
                         Function<String, T> responseMapper, String providerName,
                         String errorMessagePrefix) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.templateResourcePath = templateResourcePath;
        this.examplesResourcePath = examplesResourcePath;
        this.responseMapper = responseMapper;
        this.providerName = providerName;
        this.errorMessagePrefix = errorMessagePrefix;
    }

    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getModelName() { return modelName; }
    public Double getTemperature() { return temperature; }
    public Integer getMaxTokens() { return maxTokens; }
    public String getTemplateResourcePath() { return templateResourcePath; }
    public String getExamplesResourcePath() { return examplesResourcePath; }
    public Function<String, T> getResponseMapper() { return responseMapper; }
    public String getProviderName() { return providerName; }
    public String getErrorMessagePrefix() { return errorMessagePrefix; }
}