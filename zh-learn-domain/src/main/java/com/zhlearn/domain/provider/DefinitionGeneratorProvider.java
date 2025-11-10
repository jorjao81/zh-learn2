package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

public interface DefinitionGeneratorProvider {
    String getName();

    String getDescription();

    ProviderType getType();

    /**
     * Generate a dictionary-style definition for the given Chinese word using AI.
     *
     * <p>This provider is used when no raw definition is available (e.g., when parsing Pleco
     * exports with missing definitions or improving Anki notes without definitions).
     *
     * @param word The Chinese word to generate a definition for
     * @return AI-generated definition in dictionary style, or null if cannot generate
     */
    Definition generateDefinition(Hanzi word);
}
