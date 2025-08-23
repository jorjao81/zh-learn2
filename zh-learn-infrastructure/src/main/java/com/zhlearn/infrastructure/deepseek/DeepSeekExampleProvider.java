package com.zhlearn.infrastructure.deepseek;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.infrastructure.common.DeepSeekConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;

import java.util.Optional;

public class DeepSeekExampleProvider implements ExampleProvider {
    
    private final GenericChatModelProvider<Example> provider;

    public DeepSeekExampleProvider() {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forExamples());
    }
    
    public DeepSeekExampleProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forExamples(apiKey, baseUrl, modelName));
    }
    
    public DeepSeekExampleProvider(String apiKey) {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forExamples(apiKey));
    }
    
    @Override
    public String getName() {
        return provider.getName();
    }
    
    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        return provider.process(word, definition);
    }
}