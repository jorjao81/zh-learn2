package com.zhlearn.cli;

import com.zhlearn.application.audio.AudioOrchestrator;
import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;
import com.zhlearn.cli.audio.SystemAudioPlayer;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Command(name = "audio-select", description = "Interactive selection of pronunciation audio from available providers")
public class AudioSelectCommand implements Runnable {

    @Parameters(index = "0", description = "Chinese word")
    private String chineseWord;

    @Parameters(index = "1", description = "Exact pinyin (tone marks)")
    private String pinyin;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        AudioOrchestrator orchestrator = new AudioOrchestrator(parent.getProviderRegistry());
        List<PronunciationCandidate> candidates = orchestrator.candidatesFor(new Hanzi(chineseWord), new Pinyin(pinyin));
        if (candidates.isEmpty()) {
            System.out.println("No pronunciation candidates found.");
            // Helpful hint when Anki export is missing (default path)
            java.nio.file.Path defaultExport = java.nio.file.Paths.get(System.getProperty("user.home"), ".zh-learn", "Chinese.txt");
            if (!java.nio.file.Files.exists(defaultExport)) {
                System.out.println("Hint: Export your Anki collection as a TSV named 'Chinese.txt' to: \n  "
                    + defaultExport.getParent().toAbsolutePath());
                System.out.println("The parser currently supports note type 'Chinese 2' (columns: 1=simplified, 2=pinyin, 3=pronunciation).");
            }
            return;
        }

        SelectionSession session = new SelectionSession(candidates, new SystemAudioPlayer());

        com.zhlearn.application.audio.PronunciationCandidate selected = new com.zhlearn.cli.audio.InteractiveAudioUI().run(session);
        if (selected != null) {
            System.out.println("Selected: " + selected.soundNotation());
        } else {
            System.out.println("Skipped.");
        }
    }
}
