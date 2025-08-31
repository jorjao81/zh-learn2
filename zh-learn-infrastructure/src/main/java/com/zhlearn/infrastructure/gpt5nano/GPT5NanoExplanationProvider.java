package com.zhlearn.infrastructure.gpt5nano;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;
import com.zhlearn.infrastructure.common.OpenAIConfig;

public class GPT5NanoExplanationProvider implements ExplanationProvider {
    
    private final GenericChatModelProvider<Explanation> provider;

    public GPT5NanoExplanationProvider() {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoExplanation());
    }
    
    public GPT5NanoExplanationProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoExplanation(apiKey, baseUrl, modelName));
    }
    
    public GPT5NanoExplanationProvider(String apiKey) {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoExplanation(apiKey));
    }
    
    @Override
    public String getName() {
        return provider.getName();
    }
    
    @Override
    public String getDescription() {
        return "OpenAI GPT-5 Nano AI-powered provider for generating detailed word explanations";
    }
    
    @Override
    public Explanation getExplanation(Hanzi word) {
        return provider.process(word);
    }
}