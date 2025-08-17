package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Definition;

public interface DefinitionProvider {
    String getName();
    Definition getDefinition(ChineseWord word);
}