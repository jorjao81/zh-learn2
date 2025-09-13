package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

import java.util.Optional;

/**
 * Provides an Anki-ready pronunciation string (e.g., "[sound:file.mp3]")
 * for a given Chinese word and its pinyin, if available.
 */
public interface AudioProvider {
    String getName();
    String getDescription();
    ProviderType getType();
    Optional<String> getPronunciation(Hanzi word, Pinyin pinyin);
}
