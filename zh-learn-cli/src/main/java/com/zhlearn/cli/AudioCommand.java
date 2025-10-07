package com.zhlearn.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "audio",
        description = "Lookup pronunciation audio by pinyin from existing Anki collection")
public class AudioCommand implements Runnable {

    @Parameters(index = "0", description = "Chinese word (for context only)")
    private String chineseWord;

    @Parameters(index = "1", description = "Exact pinyin to match (with tone marks)")
    private String pinyin;

    @Option(
            names = {"--audio-provider"},
            description = "Audio provider name (default: anki)",
            defaultValue = "anki")
    private String audioProvider;

    @picocli.CommandLine.ParentCommand private MainCommand parent;

    @Override
    public void run() {
        AudioProvider selectedAudioProvider = resolveAudioProvider(audioProvider);

        WordAnalysisServiceImpl service =
                new WordAnalysisServiceImpl(
                        parent.createExampleProvider(null), // Use default
                        parent.createExplanationProvider(null), // Use default
                        parent.createDecompositionProvider(null), // Use default
                        parent.createPinyinProvider(null), // Use default
                        parent.createDefinitionProvider(null), // Use default
                        parent.createDefinitionFormatterProvider(null), // Use default
                        selectedAudioProvider);

        Hanzi word = new Hanzi(chineseWord);
        Pinyin p = new Pinyin(pinyin);

        Optional<Path> result = service.getPronunciation(word, p, audioProvider);
        if (result.isPresent()) {
            Path file = result.get();
            System.out.println(file.toAbsolutePath());
        } else {
            System.out.println("(no pronunciation)");
            Path defaultExport =
                    Path.of(System.getProperty("user.home"), ".zh-learn", "Chinese.txt");
            if (!Files.exists(defaultExport)) {
                System.out.println(
                        "Hint: Export your Anki collection as a TSV named 'Chinese.txt' to: \n  "
                                + defaultExport.getParent().toAbsolutePath());
                System.out.println(
                        "The parser currently supports note type 'Chinese 2' (columns: 1=simplified, 2=pinyin, 3=pronunciation).");
            }
        }
    }

    private AudioProvider resolveAudioProvider(String providerName) {
        return parent.getAudioProviders().stream()
                .filter(provider -> provider.getName().equals(providerName))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unknown audio provider: " + providerName));
    }
}
