package com.zhlearn.infrastructure.qwen3;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.common.DashScopeConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;

public class Qwen3FlashStructuralDecompositionProvider implements StructuralDecompositionProvider {

    private final GenericChatModelProvider<StructuralDecomposition> provider;

    public Qwen3FlashStructuralDecompositionProvider() {
        this.provider = new GenericChatModelProvider<>(DashScopeConfig.forStructuralDecomposition("qwen3-flash", "qwen-turbo-latest"));
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() { return "Qwen3 Flash (DashScope) structural decomposition provider"; }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) { return provider.process(word); }
}
