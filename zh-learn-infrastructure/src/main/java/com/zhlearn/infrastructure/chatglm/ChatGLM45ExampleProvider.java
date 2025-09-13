package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.infrastructure.common.ChatGLMConfig;
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;
import com.zhlearn.infrastructure.common.ProviderConfig;

import java.util.Optional;

public class ChatGLM45ExampleProvider implements ExampleProvider {

    private static final String MODEL = "glm-4.5";

    private final ZhipuChatModelProvider<Example> provider;

    public ChatGLM45ExampleProvider() {
        ProviderConfig<Example> base = ChatGLMConfig.forExamples();
        ProviderConfig<Example> cfg = new ProviderConfig<>(
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
        return "ChatGLM (z.ai) provider for generating contextual usage examples (glm-4.5)";
    }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        return provider.process(word, definition);
    }
}
