package com.zhlearn.application.service;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AudioProviderRegistrationTest {

    static class FakeAudioProvider implements AudioProvider {
        @Override public String getName() { return "fake-audio"; }
        @Override public String getDescription() { return "Fake audio provider for tests"; }
        @Override public ProviderType getType() { return ProviderType.DUMMY; }
        @Override public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) {
            Path audio = Path.of("src/test/resources/fixtures/audio/sample.mp3").toAbsolutePath();
            return Optional.of(audio);
        }
    }

    @Test
    void registryRegistersAndListsAudioProvider() {
        ProviderRegistry registry = new ProviderRegistry();
        registry.registerAudioProvider(new FakeAudioProvider());

        assertThat(registry.getAvailableAudioProviders()).contains("fake-audio");
        assertThat(registry.getAllProviderInfo())
            .anySatisfy(info -> {
                if (info.name().equals("fake-audio")) {
                    assertThat(info.supportedClasses()).extracting(Enum::name).contains("AUDIO");
                }
            });
    }

    @Test
    void serviceDelegatesToAudioProvider() {
        ProviderRegistry registry = new ProviderRegistry();
        WordAnalysisServiceImpl service = new WordAnalysisServiceImpl(registry);

        service.addAudioProvider("fake-audio", new FakeAudioProvider());

        Optional<Path> result = service.getPronunciation(new Hanzi("学"), new Pinyin("xué"), "fake-audio");
        assertThat(result).isPresent();
        assertThat(result.get().getFileName().toString()).isEqualTo("sample.mp3");
    }
}
