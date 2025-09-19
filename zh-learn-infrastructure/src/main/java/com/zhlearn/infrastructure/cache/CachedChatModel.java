package com.zhlearn.infrastructure.cache;

import com.zhlearn.infrastructure.common.ProviderConfig;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CachedChatModel implements ChatModel {
    private static final Logger log = LoggerFactory.getLogger(CachedChatModel.class);
    
    private final ChatModel delegate;
    private final FileSystemCache cache;
    private final String baseUrl;
    private final String modelName;
    private final Double temperature;
    private final Integer maxTokens;
    
    public CachedChatModel(ChatModel delegate, String baseUrl, String modelName, Double temperature, Integer maxTokens) {
        this.delegate = delegate;
        this.cache = new FileSystemCache();
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public <T> CachedChatModel(ChatModel baseChatModel, ProviderConfig<T> config) {
        this(baseChatModel, config.baseUrl(), config.modelName(), config.temperature(), config.maxTokens());
    }

    @Override
    public String chat(String prompt) {
        String cacheKey = CacheKeyGenerator.generateKey(prompt, baseUrl, modelName, temperature, maxTokens);

        Optional<String> cachedResponse = cache.get(cacheKey);
        if (cachedResponse.isPresent()) {
            log.debug("Using cached response for prompt");
            return cachedResponse.get();
        }
        
        log.debug("Cache miss, calling AI model");
        String response = delegate.chat(prompt);
        
        if (response != null) {
            cache.put(cacheKey, response);
        }
        
        return response;
    }
}
