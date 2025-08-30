package com.zhlearn.domain.dictionary;

import com.zhlearn.domain.model.WordAnalysis;
import java.util.Optional;

public interface Dictionary {
    String getName();
    Optional<WordAnalysis> lookup(String simplifiedCharacters);
}