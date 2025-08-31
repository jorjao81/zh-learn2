package com.zhlearn.infrastructure.deepseek;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.infrastructure.common.DeepSeekConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;

public class DeepSeekExplanationProvider implements ExplanationProvider {
    
    private final GenericChatModelProvider<Explanation> provider;

    public DeepSeekExplanationProvider() {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forExplanation());
    }
    
    public DeepSeekExplanationProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forExplanation(apiKey, baseUrl, modelName));
    }
    
    public DeepSeekExplanationProvider(String apiKey) {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forExplanation(apiKey));
    }
    
    @Override
    public String getName() {
        return provider.getName();
    }
    
    @Override
    public String getDescription() {
        return "DeepSeek AI-powered explanation provider for generating detailed word explanations";
    }
    
    @Override
    public Explanation getExplanation(Hanzi word) {
        return provider.process(word);
    }
}