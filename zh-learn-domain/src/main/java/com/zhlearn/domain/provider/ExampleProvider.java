package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;

import java.util.Optional;

public interface ExampleProvider {
    String getName();
    String getDescription();
    Example getExamples(Hanzi word, Optional<String> definition);
}