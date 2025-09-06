package com.zhlearn.infrastructure.anki;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExistingAnkiPronunciationProviderTest {

    @Test
    void returnsPronunciationForExactPinyinMatch() {
        String content = """
            Chinese\t学\txué\t[sound:xue.mp3]\tdef
            Chinese 2\t习\txí\t[sound:xi.mp3]\tdef
            Other\t词\tcí\t[sound:ci.mp3]\tdef
            """;
        ExistingAnkiPronunciationProvider provider =
            ExistingAnkiPronunciationProvider.fromString(content, new AnkiCollectionParser());

        Optional<String> result = provider.getPronunciation(new Hanzi("学"), new Pinyin("xué"));
        assertThat(result).contains("[sound:xue.mp3]");

        Optional<String> result2 = provider.getPronunciation(new Hanzi("习"), new Pinyin("xí"));
        assertThat(result2).contains("[sound:xi.mp3]");

        Optional<String> noMatch = provider.getPronunciation(new Hanzi("词"), new Pinyin("cí"));
        assertThat(noMatch).isEmpty();
    }

    @Test
    void ignoresEmptyPronunciationAndKeepsFirstNonEmpty() {
        List<AnkiCollectionNote> notes = List.of(
            new AnkiCollectionNote("Chinese", "学", "xué", "", "", "", "", ""),
            new AnkiCollectionNote("Chinese", "学", "xué", "[sound:xue-2.mp3]", "", "", "", ""),
            new AnkiCollectionNote("Chinese", "学", "xué", "[sound:xue-3.mp3]", "", "", "", "")
        );

        ExistingAnkiPronunciationProvider provider = new ExistingAnkiPronunciationProvider(notes);
        Optional<String> result = provider.getPronunciation(new Hanzi("学"), new Pinyin("xué"));
        assertThat(result).contains("[sound:xue-2.mp3]");
    }

    @Test
    void returnsEmptyWhenNoPronunciationFound() {
        List<AnkiCollectionNote> notes = List.of(
            new AnkiCollectionNote("Chinese", "学", "xué", "", "", "", "", "")
        );

        ExistingAnkiPronunciationProvider provider = new ExistingAnkiPronunciationProvider(notes);
        Optional<String> result = provider.getPronunciation(new Hanzi("学"), new Pinyin("xué"));
        assertThat(result).isEmpty();
    }
}

