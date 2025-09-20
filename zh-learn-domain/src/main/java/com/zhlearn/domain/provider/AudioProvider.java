package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Provides a pronunciation audio file for a given Chinese word and its pinyin, if available.
 */
public interface AudioProvider {
    String getName();
    String getDescription();
    ProviderType getType();
    /**
     * Return a single pronunciation, typically the best or default choice.
     *
     * Existing providers may implement only this method. For providers capable of
     * returning multiple options (e.g., Forvo), override {@link #getPronunciations(Hanzi, Pinyin)}
     * and optionally keep this method to return the first/best item.
     */
    Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin);

    /**
     * Return zero or more pronunciations for the given input. By default this
     * wraps {@link #getPronunciation(Hanzi, Pinyin)} if only a single result is
     * available. Implementors that can supply multiple options should override
     * this method.
     */
    default List<Path> getPronunciations(Hanzi word, Pinyin pinyin) {
        return getPronunciation(word, pinyin).map(List::of).orElse(List.of());
    }

    /**
     * Return zero or more pronunciations with descriptions for the given input.
     * Default implementation converts getPronunciations results to descriptions
     * using filenames. Providers should override to supply custom descriptions.
     */
    default List<PronunciationDescription> getPronunciationsWithDescriptions(Hanzi word, Pinyin pinyin) {
        return getPronunciations(word, pinyin).stream()
            .map(path -> new PronunciationDescription(path, null))
            .toList();
    }

    /**
     * Simple record to hold pronunciation path and optional description.
     */
    record PronunciationDescription(Path path, String description) {
    }
}
