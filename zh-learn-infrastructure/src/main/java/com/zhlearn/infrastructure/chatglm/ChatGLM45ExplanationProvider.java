package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.infrastructure.common.ChatGLMConfig;
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;
import com.zhlearn.infrastructure.common.ProviderConfig;

public class ChatGLM45ExplanationProvider implements ExplanationProvider {

    private static final String MODEL = "glm-4.5";

    private final ZhipuChatModelProvider<Explanation> provider;

    public ChatGLM45ExplanationProvider() {
        ProviderConfig<Explanation> base = ChatGLMConfig.forExplanation();
        ProviderConfig<Explanation> cfg = new ProviderConfig<>(
            base.getApiKey(),
            base.getBaseUrl(),
            MODEL,
            base.getTemperature(),
            base.getMaxTokens(),
            base.getTemplateResourcePath(),
            base.getExamplesResourcePath(),
            base.getResponseMapper(),
            MODEL,
            base.getErrorMessagePrefix()
        );
        this.provider = new ZhipuChatModelProvider<>(cfg);
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() {
        return "ChatGLM (z.ai) provider for detailed explanations (glm-4.5)";
    }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public Explanation getExplanation(Hanzi word) { return provider.process(word); }
}
