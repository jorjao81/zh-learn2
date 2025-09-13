package com.zhlearn.infrastructure.qwen3;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.common.DashScopeConfig;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;

public class Qwen3PlusStructuralDecompositionProvider implements StructuralDecompositionProvider {

    private final GenericChatModelProvider<StructuralDecomposition> provider;

    public Qwen3PlusStructuralDecompositionProvider() {
        this.provider = new GenericChatModelProvider<>(DashScopeConfig.forStructuralDecomposition("qwen3-plus", "qwen-plus-latest"));
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() { return "Qwen3 Plus (DashScope) structural decomposition provider"; }

    @Override
    public ProviderType getType() { return ProviderType.AI; }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) { return provider.process(word); }
}
