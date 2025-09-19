package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.common.ChatGLMConfig;
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;
import com.zhlearn.infrastructure.common.ProviderConfig;

public class ChatGLM45StructuralDecompositionProvider implements StructuralDecompositionProvider {

    private static final String MODEL = "glm-4.5";

    private final ZhipuChatModelProvider<StructuralDecomposition> provider;

    public ChatGLM45StructuralDecompositionProvider() {
        ProviderConfig<StructuralDecomposition> base = ChatGLMConfig.forStructuralDecomposition();
        ProviderConfig<StructuralDecomposition> cfg = new ProviderConfig<>(
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

    public ChatGLM45StructuralDecompositionProvider(String apiKey) {
        ProviderConfig<StructuralDecomposition> base = ChatGLMConfig.forStructuralDecomposition();
        ProviderConfig<StructuralDecomposition> cfg = new ProviderConfig<>(
            apiKey,
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
        return "ChatGLM (z.ai) provider for structural decomposition (glm-4.5)";
    }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) { return provider.process(word); }
}
