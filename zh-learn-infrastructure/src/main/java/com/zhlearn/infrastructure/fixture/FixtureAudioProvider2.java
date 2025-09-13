package com.zhlearn.infrastructure.fixture;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;

import java.util.Optional;

/**
 * Alternate fixture provider returning a different sample clip.
 */
public class FixtureAudioProvider2 implements AudioProvider {
    @Override
    public String getName() { return "fixture-audio-2"; }

    @Override
    public String getDescription() { return "Fixture provider that returns sample2.mp3 for any term"; }

    @Override
    public ProviderType getType() { return ProviderType.DUMMY; }

    @Override
    public Optional<String> getPronunciation(Hanzi word, Pinyin pinyin) {
        return Optional.of("[sound:sample2.mp3]");
    }
}

