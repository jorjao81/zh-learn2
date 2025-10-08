package com.zhlearn.domain.provider;

import java.util.Optional;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

public interface DefinitionFormatterProvider {
    String getName();

    String getDescription();

    ProviderType getType();

    /**
     * Format or generate a definition for the given Chinese word using AI.
     *
     * @param word The Chinese word to format a definition for
     * @param rawDefinition Optional raw definition (typically from Pleco export). If present, AI
     *     will format it. If empty, AI may generate new definition.
     * @return AI-formatted HTML definition, or null if cannot process
     */
    Definition formatDefinition(Hanzi word, Optional<String> rawDefinition);
}
