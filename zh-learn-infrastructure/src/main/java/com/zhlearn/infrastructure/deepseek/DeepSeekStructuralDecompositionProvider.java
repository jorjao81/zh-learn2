package com.zhlearn.infrastructure.deepseek;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.common.DeepSeekConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;

public class DeepSeekStructuralDecompositionProvider implements StructuralDecompositionProvider {
    
    private final GenericChatModelProvider<StructuralDecomposition> provider;

    public DeepSeekStructuralDecompositionProvider() {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forStructuralDecomposition());
    }
    
    public DeepSeekStructuralDecompositionProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forStructuralDecomposition(apiKey, baseUrl, modelName));
    }
    
    public DeepSeekStructuralDecompositionProvider(String apiKey) {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forStructuralDecomposition(apiKey));
    }
    
    @Override
    public String getName() {
        return provider.getName();
    }
    
    @Override
    public String getDescription() {
        return "DeepSeek AI-powered provider for analyzing character structure and radical decomposition";
    }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return provider.process(word);
    }
}