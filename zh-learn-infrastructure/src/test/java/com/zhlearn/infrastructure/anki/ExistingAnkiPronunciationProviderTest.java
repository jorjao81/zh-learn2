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
            Chinese 2\t学\txué\t[sound:xue.mp3]\tdef
            Chinese 2\t习\txí\t[sound:xi.mp3]\tdef
            Other\t词\tcí\t[sound:ci.mp3]\tdef
            """;
        ExistingAnkiPronunciationProvider provider =
            ExistingAnkiPronunciationProvider.fromString(content);

        Optional<String> result = provider.getPronunciation(new Hanzi("学"), new Pinyin("xué"));
        assertThat(result).contains("[sound:xue.mp3]");

        Optional<String> result2 = provider.getPronunciation(new Hanzi("习"), new Pinyin("xí"));
        assertThat(result2).contains("[sound:xi.mp3]");

        Optional<String> noMatch = provider.getPronunciation(new Hanzi("词"), new Pinyin("cí"));
        assertThat(noMatch).isEmpty();
    }

    @Test
    void ignoresEmptyPronunciationAndKeepsFirstNonEmpty() {
        List<AnkiNote> notes = List.of(
            AnkiNote.ofCollection("Chinese", "xué", "学", "", "", "", "", "", "", "", "", ""),
            AnkiNote.ofCollection("Chinese", "xué", "学", "[sound:xue-2.mp3]", "", "", "", "", "", "", "", ""),
            AnkiNote.ofCollection("Chinese", "xué", "学", "[sound:xue-3.mp3]", "", "", "", "", "", "", "", "")
        );

        ExistingAnkiPronunciationProvider provider = new ExistingAnkiPronunciationProvider(notes);
        Optional<String> result = provider.getPronunciation(new Hanzi("学"), new Pinyin("xué"));
        assertThat(result).contains("[sound:xue-2.mp3]");
    }

    @Test
    void returnsEmptyWhenNoPronunciationFound() {
        List<AnkiNote> notes = List.of(
            AnkiNote.ofCollection("Chinese", "xué", "学", "", "", "", "", "", "", "", "", "")
        );

        ExistingAnkiPronunciationProvider provider = new ExistingAnkiPronunciationProvider(notes);
        Optional<String> result = provider.getPronunciation(new Hanzi("学"), new Pinyin("xué"));
        assertThat(result).isEmpty();
    }

    @Test
    void shouldMatchPinyinFromActualFiles() {
        // Test with actual content from Chinese.txt to reproduce the real issue
        // This is a real entry from Chinese.txt that contains dǎo
        String chineseTxtContent = """
            Chinese 2	岛屿	dǎoyǔ	[sound:岛屿_normalized.mp3]	islands and islets; islands
            Chinese 2	导航	dǎoháng	[sound:导航_normalized.mp3]	navigation
            Chinese 2	倒霉	dǎoméi	[sound:倒霉_normalized-eee6e3ca128fe52b60127beb231a29de58ee9ce6.mp3]	azar, azarado
            """;
        
        ExistingAnkiPronunciationProvider provider = 
            ExistingAnkiPronunciationProvider.fromString(chineseTxtContent);

        // Test the conversion from flash2.tsv format
        String[] testCases = {"dao3", "daohang2", "daomei2"};
        
        for (String originalPinyin : testCases) {
            String convertedPinyin = com.zhlearn.pinyin.PinyinToneConverter.convertToToneMarks(originalPinyin);
            System.out.println("Testing: '" + originalPinyin + "' -> '" + convertedPinyin + "'");
            
            // Try to find audio for the converted pinyin
            Optional<String> result = provider.getPronunciation(new Hanzi("test"), new Pinyin(convertedPinyin));
            if (result.isPresent()) {
                System.out.println("  Found: " + result.get());
            } else {
                System.out.println("  Not found");
            }
        }

        // This specific test should pass if normalization is working
        String convertedDao = com.zhlearn.pinyin.PinyinToneConverter.convertToToneMarks("dao3yu3");
        Optional<String> result = provider.getPronunciation(new Hanzi("岛屿"), new Pinyin(convertedDao));
        
        System.out.println("Final test: 'dao3yu3' -> '" + convertedDao + "'");
        System.out.println("Looking for match with: 'dǎoyǔ'");
        System.out.println("Result: " + result);
        
        // This will show us if there's still a mismatch
        assertThat(result).as("Should find audio for dǎoyǔ").isNotEmpty();
    }
}
