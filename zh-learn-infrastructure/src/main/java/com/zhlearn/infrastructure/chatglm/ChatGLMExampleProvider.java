package com.zhlearn.infrastructure.chatglm;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.infrastructure.common.ChatGLMConfig;
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;

import java.util.Optional;

public class ChatGLMExampleProvider implements ExampleProvider {

    private final ZhipuChatModelProvider<Example> provider;

    public ChatGLMExampleProvider() {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forExamples());
    }

    public ChatGLMExampleProvider(String apiKey, String baseUrl, String modelName) {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forExamples(apiKey, baseUrl, modelName));
    }

    public ChatGLMExampleProvider(String apiKey) {
        this.provider = new ZhipuChatModelProvider<>(ChatGLMConfig.forExamples(apiKey));
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() {
        return "ChatGLM (z.ai) provider for generating contextual usage examples";
    }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        return provider.process(word, definition);
    }
}
