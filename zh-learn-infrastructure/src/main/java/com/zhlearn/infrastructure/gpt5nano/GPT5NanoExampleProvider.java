package com.zhlearn.infrastructure.gpt5nano;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;
import com.zhlearn.infrastructure.common.OpenAIConfig;

import java.util.Optional;

public class GPT5NanoExampleProvider implements ExampleProvider {
    
    private final GenericChatModelProvider<Example> provider;

    public GPT5NanoExampleProvider() {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoExamples());
    }
    
    public GPT5NanoExampleProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoExamples(apiKey, baseUrl, modelName));
    }
    
    public GPT5NanoExampleProvider(String apiKey) {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoExamples(apiKey));
    }
    
    @Override
    public String getName() {
        return provider.getName();
    }
    
    @Override
    public String getDescription() {
        return "OpenAI GPT-5 Nano AI-powered provider for generating contextual usage examples";
    }
    
    @Override
    public ProviderType getType() { return ProviderType.AI; }
    
    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        return provider.process(word, definition);
    }
}
