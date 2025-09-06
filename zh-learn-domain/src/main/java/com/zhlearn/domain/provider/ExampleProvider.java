package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

import java.util.Optional;

public interface ExampleProvider {
    String getName();
    String getDescription();
    ProviderType getType();
    Example getExamples(Hanzi word, Optional<String> definition);
}
