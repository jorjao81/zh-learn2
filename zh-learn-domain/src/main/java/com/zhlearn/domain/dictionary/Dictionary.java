package com.zhlearn.domain.dictionary;

import java.util.Optional;

import com.zhlearn.domain.model.WordAnalysis;

public interface Dictionary {
    String getName();

    Optional<WordAnalysis> lookup(String simplifiedCharacters);
}
