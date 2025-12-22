package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.DefinitionProvider;

public class DictionaryDefinitionProvider implements DefinitionProvider {
    private final Dictionary dictionary;

    public DictionaryDefinitionProvider(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String getName() {
        // Use the dictionary's name directly (e.g., "pleco-export")
        return dictionary.getName();
    }

    @Override
    public String getDescription() {
        return "Dictionary-based definition provider using "
                + dictionary.getName()
                + " dictionary data";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DICTIONARY;
    }

    @Override
    public Definition getDefinition(Hanzi word) {
        return dictionary
                .lookup(word.characters())
                .map(
                        analysis -> {
                            // If the dictionary definition is a placeholder for empty definitions,
                            // return null to trigger AI generation
                            String defText = analysis.definition().meaning();
                            if (defText != null && defText.startsWith("[No definition available")) {
                                return null;
                            }
                            return analysis.definition();
                        })
                .orElse(null);
    }
}
