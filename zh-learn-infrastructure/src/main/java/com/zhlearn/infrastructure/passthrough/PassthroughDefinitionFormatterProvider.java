package com.zhlearn.infrastructure.passthrough;

import java.util.Optional;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;

/**
 * A passthrough definition formatter that returns the definition unchanged. Used when preserving
 * original definition formatting from source data (e.g., Anki exports) without applying any
 * AI-based formatting transformations.
 */
public class PassthroughDefinitionFormatterProvider implements DefinitionFormatterProvider {

    @Override
    public String getName() {
        return "passthrough";
    }

    @Override
    public String getDescription() {
        return "Passthrough formatter that returns definitions unchanged";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DICTIONARY;
    }

    @Override
    public Definition formatDefinition(Hanzi word, Optional<String> rawDefinition) {
        // Return definition as-is without any formatting
        if (rawDefinition.isPresent()) {
            return new Definition(rawDefinition.get());
        }
        return new Definition("[No definition available]");
    }
}
