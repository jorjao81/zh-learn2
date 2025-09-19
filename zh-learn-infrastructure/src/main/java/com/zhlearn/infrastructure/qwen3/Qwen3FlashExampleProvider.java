package com.zhlearn.infrastructure.qwen3;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.infrastructure.common.DashScopeConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;

public class Qwen3FlashExampleProvider implements ExampleProvider {

    private final GenericChatModelProvider<Example> provider;

    public Qwen3FlashExampleProvider() {
        this.provider = new GenericChatModelProvider<>(DashScopeConfig.forExamples("qwen3-flash", "qwen-turbo-latest"));
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() { return "Qwen3 Flash (DashScope) example provider"; }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public Example getExamples(Hanzi word, java.util.Optional<String> definition) {
        return provider.process(word, definition);
    }
}
