package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.infrastructure.common.ChatGLMConfig;
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;

public class ChatGLMExplanationProvider implements ExplanationProvider {

    private final ZhipuChatModelProvider<Explanation> provider;

    public ChatGLMExplanationProvider() {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forExplanation());
    }

    public ChatGLMExplanationProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forExplanation(apiKey, baseUrl, modelName));
    }

    public ChatGLMExplanationProvider(String apiKey) {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forExplanation(apiKey));
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() {
        return "ChatGLM (z.ai) provider for detailed explanations in Chinese and English";
    }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public Explanation getExplanation(Hanzi word) { return provider.process(word); }
}
