package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.common.ChatGLMConfig;
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;

public class ChatGLMStructuralDecompositionProvider implements StructuralDecompositionProvider {

    private final ZhipuChatModelProvider<StructuralDecomposition> provider;

    public ChatGLMStructuralDecompositionProvider() {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forStructuralDecomposition());
    }

    public ChatGLMStructuralDecompositionProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forStructuralDecomposition(apiKey, baseUrl, modelName));
    }

    public ChatGLMStructuralDecompositionProvider(String apiKey) {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forStructuralDecomposition(apiKey));
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() {
        return "ChatGLM (z.ai) provider for structural decomposition of Chinese characters";
    }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) { return provider.process(word); }
}
