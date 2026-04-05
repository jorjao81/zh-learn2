package com.zhlearn.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zhlearn.application.audio.AudioOrchestrator;
import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;
import com.zhlearn.cli.audio.InteractiveAudioUI;
import com.zhlearn.cli.audio.SystemAudioPlayer;
import com.zhlearn.cli.util.AudioSelectionUtils;
import com.zhlearn.cli.util.AudioSelectionUtils.AudioSelection;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/** CLI command to enrich Chinese explanation markdown with pronunciation audio. */
@Command(
        name = "enrich-explanations",
        description = "Add pronunciation audio to Chinese explanation markdown entries")
public class EnrichExplanationsCommand implements Runnable {

    private static final Pattern H1_PATTERN =
            Pattern.compile("^#\\s+(\\S+(?:\\s+\\S+)*)\\s+Analysis:\\s*(.+)$");

    @Parameters(index = "0", description = "Path to explanations markdown file")
    private Path inputPath;

    @Option(
            names = {"-o", "--output"},
            description = "Output file path (default: overwrite input file)")
    private Path outputPath;

    @Option(
            names = {"--dry-run"},
            description =
                    "Parse and report which entries need audio, without downloading or modifying")
    private boolean dryRun = false;

    @Option(
            names = {"--audio-selections"},
            description =
                    "Pre-configured audio selections (format: term:provider:description;term:provider:description)")
    private String audioSelectionsParam;

    @ParentCommand private MainCommand parent;

    @Override
    public void run() {
        if (!Files.isRegularFile(inputPath)) {
            System.err.println("File not found: " + inputPath);
            System.exit(1);
        }

        String content = readFile(inputPath);
        String[] lines = content.split("\n", -1);

        // Pass 1: identify entries needing audio
        List<EntryToEnrich> entriesToEnrich = findEntriesNeedingAudio(lines);

        System.out.println("Total entries needing audio: " + entriesToEnrich.size());
        for (EntryToEnrich entry : entriesToEnrich) {
            System.out.println("  " + entry.term + " (" + entry.pinyin + ")");
        }

        if (dryRun || entriesToEnrich.isEmpty()) {
            if (dryRun) System.out.println("\nDry run - no changes made.");
            return;
        }

        // Resolve audio output directory
        Path audioDir = inputPath.getParent().resolve("audio");
        try {
            Files.createDirectories(audioDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create audio directory: " + audioDir, e);
        }

        // Download audio for each entry via interactive selection
        Map<String, AudioSelection> preSelections =
                AudioSelectionUtils.parseAudioSelections(audioSelectionsParam);
        AudioOrchestrator orchestrator =
                new AudioOrchestrator(
                        parent.getAudioProviders(), parent.getAudioExecutor().getExecutor());

        Map<String, Path> audioFiles = new HashMap<>(); // term -> local audio path

        for (int i = 0; i < entriesToEnrich.size(); i++) {
            EntryToEnrich entry = entriesToEnrich.get(i);
            System.out.println(
                    "\n["
                            + (i + 1)
                            + "/"
                            + entriesToEnrich.size()
                            + "] "
                            + entry.term
                            + " ("
                            + entry.pinyin
                            + ")");

            Path selected = selectAudio(orchestrator, entry, preSelections);
            if (selected != null) {
                // Copy to ./audio/
                Path localPath = audioDir.resolve(selected.getFileName());
                try {
                    Files.copy(selected, localPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("  Failed to copy audio: " + e.getMessage());
                    continue;
                }
                audioFiles.put(entry.term, localPath);
                System.out.println("  -> " + localPath.getFileName());
            } else {
                System.out.println("  Skipped.");
            }
        }

        // Pass 2: insert ## Pronunciation sections
        String enriched = insertPronunciationSections(lines, audioFiles);

        // Write output
        Path output = outputPath != null ? outputPath : inputPath;
        try {
            Files.writeString(output, enriched, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write output: " + output, e);
        }

        System.out.println(
                "\nEnriched " + audioFiles.size() + " entries with audio. Written to: " + output);
    }

    private Path selectAudio(
            AudioOrchestrator orchestrator,
            EntryToEnrich entry,
            Map<String, AudioSelection> preSelections) {
        Hanzi word = new Hanzi(entry.term);
        Pinyin pin = new Pinyin(entry.pinyin);

        List<PronunciationCandidate> raw = orchestrator.candidatesFor(word, pin);
        List<PronunciationCandidate> candidates =
                parent.getPrePlayback().preprocessCandidates(word, pin, raw);

        if (candidates.isEmpty()) {
            System.out.println("  No candidates found.");
            return null;
        }

        System.out.println("  Found " + candidates.size() + " candidates.");

        // Check pre-configured selection
        AudioSelection preSel = preSelections.get(entry.term);
        if (preSel != null) {
            PronunciationCandidate match =
                    AudioSelectionUtils.findMatchingCandidate(candidates, preSel);
            if (match != null) {
                System.out.println(
                        "  Auto-selected: " + match.label() + " / " + match.description());
                return match.file();
            }
            System.out.println(
                    "  Pre-configured selection not found ("
                            + preSel.provider()
                            + ":"
                            + preSel.description()
                            + "), falling back to interactive.");
        }

        // Interactive selection
        SelectionSession session =
                new SelectionSession(
                        candidates, new SystemAudioPlayer(parent.getAnkiMediaLocator()));
        PronunciationCandidate selected = new InteractiveAudioUI().run(session, word, pin);
        return selected != null ? selected.file() : null;
    }

    /** Scan lines to find entries that have ## Pinyin but no ## Pronunciation after it. */
    private List<EntryToEnrich> findEntriesNeedingAudio(String[] lines) {
        List<EntryToEnrich> result = new ArrayList<>();
        String currentTerm = null;
        String currentPinyin = null;
        boolean inPinyinSection = false;
        boolean hasPronunciation = false;

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();

            // Detect new entry
            Matcher h1 = H1_PATTERN.matcher(trimmed);
            if (h1.matches()) {
                // Finalize previous entry
                if (currentTerm != null && currentPinyin != null && !hasPronunciation) {
                    result.add(new EntryToEnrich(currentTerm, currentPinyin));
                }
                currentTerm = h1.group(2).trim();
                currentPinyin = null;
                inPinyinSection = false;
                hasPronunciation = false;
                continue;
            }

            if (trimmed.equals("## Pinyin")) {
                inPinyinSection = true;
                continue;
            }

            if (inPinyinSection) {
                if (trimmed.startsWith("## ")) {
                    inPinyinSection = false;
                    if (trimmed.equals("## Pronunciation")) {
                        hasPronunciation = true;
                    }
                } else if (!trimmed.isEmpty() && currentPinyin == null) {
                    currentPinyin = trimmed;
                }
                continue;
            }

            if (trimmed.equals("## Pronunciation")) {
                hasPronunciation = true;
            }
        }

        // Finalize last entry
        if (currentTerm != null && currentPinyin != null && !hasPronunciation) {
            result.add(new EntryToEnrich(currentTerm, currentPinyin));
        }

        return result;
    }

    /** Insert ## Pronunciation sections into lines for entries that have audio. */
    private String insertPronunciationSections(String[] lines, Map<String, Path> audioFiles) {
        StringBuilder output = new StringBuilder();
        String currentTerm = null;
        String currentPinyin = null;
        boolean inPinyinSection = false;

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();

            // Detect new entry
            Matcher h1 = H1_PATTERN.matcher(trimmed);
            if (h1.matches()) {
                currentTerm = h1.group(2).trim();
                currentPinyin = null;
                inPinyinSection = false;
                output.append(lines[i]).append("\n");
                continue;
            }

            if (trimmed.equals("## Pinyin")) {
                inPinyinSection = true;
                output.append(lines[i]).append("\n");
                continue;
            }

            if (inPinyinSection && trimmed.startsWith("## ")) {
                inPinyinSection = false;

                // Check if we should insert pronunciation before this heading
                if (!trimmed.equals("## Pronunciation")
                        && currentTerm != null
                        && audioFiles.containsKey(currentTerm)) {
                    Path audioPath = audioFiles.get(currentTerm);
                    String relativePath = "audio/" + audioPath.getFileName();
                    String pinyin = currentPinyin != null ? currentPinyin : currentTerm;

                    output.append("## Pronunciation\n");
                    output.append("\n");
                    output.append("[")
                            .append(pinyin)
                            .append("](")
                            .append(relativePath)
                            .append(")\n");
                    output.append("\n");
                }

                output.append(lines[i]).append("\n");
                continue;
            }

            if (inPinyinSection && !trimmed.isEmpty() && currentPinyin == null) {
                currentPinyin = trimmed;
            }

            output.append(lines[i]).append("\n");
        }

        // Remove trailing extra newline if original didn't have one
        String result = output.toString();
        if (!result.isEmpty() && result.endsWith("\n\n") && !lines[lines.length - 1].isEmpty()) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String readFile(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + path, e);
        }
    }

    private record EntryToEnrich(String term, String pinyin) {}
}
