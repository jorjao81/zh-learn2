package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Provides an Anki-ready pronunciation string (e.g., "[sound:file.mp3]")
 * for a given Chinese word and its pinyin, if available.
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
    Optional<String> getPronunciation(Hanzi word, Pinyin pinyin);

    /**
     * Return zero or more pronunciations for the given input. By default this
     * wraps {@link #getPronunciation(Hanzi, Pinyin)} if only a single result is
     * available. Implementors that can supply multiple options should override
     * this method.
     */
    default List<String> getPronunciations(Hanzi word, Pinyin pinyin) {
        return getPronunciation(word, pinyin).map(List::of).orElse(List.of());
    }
}
