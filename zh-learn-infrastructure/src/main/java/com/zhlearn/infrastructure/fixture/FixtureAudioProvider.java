package com.zhlearn.infrastructure.fixture;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Development-only provider that always returns a sample audio reference.
 */
public class FixtureAudioProvider implements AudioProvider {
    @Override
    public String getName() { return "fixture-audio"; }

    @Override
    public String getDescription() { return "Fixture provider that returns a sample.mp3 reference for any term"; }

    @Override
    public ProviderType getType() { return ProviderType.DUMMY; }

    @Override
    public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) {
        return Optional.of(Path.of("sample.mp3"));
    }
}
