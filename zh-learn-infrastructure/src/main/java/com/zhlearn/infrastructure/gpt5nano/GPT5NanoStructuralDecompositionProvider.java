package com.zhlearn.infrastructure.gpt5nano;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;
import com.zhlearn.infrastructure.common.OpenAIConfig;

public class GPT5NanoStructuralDecompositionProvider implements StructuralDecompositionProvider {
    
    private final GenericChatModelProvider<StructuralDecomposition> provider;

    public GPT5NanoStructuralDecompositionProvider() {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoStructuralDecomposition());
    }
    
    public GPT5NanoStructuralDecompositionProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoStructuralDecomposition(apiKey, baseUrl, modelName));
    }
    
    public GPT5NanoStructuralDecompositionProvider(String apiKey) {
        this.provider = new GenericChatModelProvider<>(OpenAIConfig.forGPT5NanoStructuralDecomposition(apiKey));
    }
    
    @Override
    public String getName() {
        return provider.getName();
    }
    
    @Override
    public String getDescription() {
        return "OpenAI GPT-5 Nano AI-powered provider for analyzing character structure and radical decomposition";
    }
    
    @Override
    public ProviderType getType() { return ProviderType.AI; }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return provider.process(word);
    }
}
