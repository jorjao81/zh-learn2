package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Definition;

public interface DefinitionProvider {
    String getName();
    String getDescription();
    Definition getDefinition(Hanzi word);
}