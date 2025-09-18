package com.zhlearn.application.audio;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.anki.AnkiPronunciationProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AnkiAudioProviderAcceptanceTest {

    private String previousFixtureFlag;

    @BeforeEach
    void saveAndClearFixtureFlag() {
        previousFixtureFlag = System.getProperty("zhlearn.enable.fixture.audio");
        System.clearProperty("zhlearn.enable.fixture.audio");
    }

    @AfterEach
    void restoreFixtureFlag() {
        if (previousFixtureFlag == null) {
            System.clearProperty("zhlearn.enable.fixture.audio");
        } else {
            System.setProperty("zhlearn.enable.fixture.audio", previousFixtureFlag);
        }
    }

    @Test
    void ankiProviderReturnsExistingAudioWhenPresent_andSkipsWhenAbsent() {
        String content = """
            Chinese 2\t学习\txuéxí\t[sound:xuexi.mp3]\tdef
            Chinese 2\t导航\tdǎoháng\t[sound:daohang.mp3]\tdef
            """;

        ProviderRegistry registry = new ProviderRegistry();
        // Override discovery with our test instance
        registry.registerAudioProvider(AnkiPronunciationProvider.fromString(content));

        WordAnalysisServiceImpl service = new WordAnalysisServiceImpl(registry);

        Optional<Path> present = service.getPronunciation(new Hanzi("学习"), new Pinyin("xuéxí"), "anki");
        assertThat(present).isPresent();
        assertThat(present.get().getFileName().toString()).isEqualTo("xuexi.mp3");

        Optional<Path> absent = service.getPronunciation(new Hanzi("不存在"), new Pinyin("búzàicún"), "anki");
        assertThat(absent).isEmpty();
    }

    @Test
    void orchestratorPrefersAnkiFirst_whenMultipleProvidersEnabled() {
        // Enable fixture providers for this test to simulate multiple results
        System.setProperty("zhlearn.enable.fixture.audio", "true");

        String content = """
            Chinese 2\t学习\txuéxí\t[sound:xuexi.mp3]\tdef
            """;

        ProviderRegistry registry = new ProviderRegistry();
        registry.registerAudioProvider(AnkiPronunciationProvider.fromString(content));
        // Also register a simple dummy fixture provider
        registry.registerAudioProvider(new AudioProvider() {
            @Override public String getName() { return "fixture-audio"; }
            @Override public String getDescription() { return "test fixture"; }
            @Override public ProviderType getType() { return ProviderType.DUMMY; }
            @Override public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) { return Optional.of(Path.of("sample.mp3")); }
        });

        AudioOrchestrator orchestrator = new AudioOrchestrator(registry);
        List<PronunciationCandidate> list = orchestrator.candidatesFor(new Hanzi("学习"), new Pinyin("xuéxí"));

        assertThat(list).isNotEmpty();
        assertThat(list.get(0).label()).isEqualTo("anki");
        // Ensure both providers contributed when enabled
        assertThat(list.stream().map(PronunciationCandidate::label).toList())
            .contains("fixture-audio");
    }
}
