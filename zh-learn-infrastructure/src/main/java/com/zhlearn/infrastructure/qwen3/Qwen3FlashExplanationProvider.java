package com.zhlearn.infrastructure.qwen3;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.infrastructure.common.DashScopeConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;

public class Qwen3FlashExplanationProvider implements ExplanationProvider {

    private final GenericChatModelProvider<Explanation> provider;

    public Qwen3FlashExplanationProvider() {
        this.provider = new GenericChatModelProvider<>(DashScopeConfig.forExplanation("qwen3-flash", "qwen-turbo-latest"));
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() { return "Qwen3 Flash (DashScope) explanation provider"; }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public Explanation getExplanation(Hanzi word) { return provider.process(word); }
}
