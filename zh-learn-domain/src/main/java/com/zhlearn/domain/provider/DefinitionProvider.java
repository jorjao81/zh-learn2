package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

public interface DefinitionProvider {
    String getName();
    String getDescription();
    ProviderType getType();
    Definition getDefinition(Hanzi word);
}
