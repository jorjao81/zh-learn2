package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;

import java.util.Optional;

public class DummyDefinitionFormatterProvider implements DefinitionFormatterProvider {

    @Override
    public String getName() {
        return "dummy";
    }

    @Override
    public String getDescription() {
        return "Test provider that returns dummy formatted definitions for development and testing";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DUMMY;
    }

    @Override
    public Definition formatDefinition(Hanzi word, Optional<String> rawDefinition) {
        String formatted = rawDefinition.orElse("dummy definition for " + word.characters());
        return new Definition("<span class=\"part-of-speech\">dummy</span> " + formatted);
    }
}