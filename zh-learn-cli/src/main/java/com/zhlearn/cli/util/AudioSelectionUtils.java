package com.zhlearn.cli.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zhlearn.application.audio.PronunciationCandidate;

/**
 * Utility methods for audio selection parsing and matching in CLI commands. Extracted from
 * ImproveAnkiCommand and ParsePlecoCommand to eliminate duplication.
 */
public final class AudioSelectionUtils {

    private AudioSelectionUtils() {}

    /** Represents a pre-configured audio selection for a word. */
    public record AudioSelection(String provider, String description) {}

    /**
     * Parse audio selections from command-line parameter format.
     *
     * @param param format: "word:provider:description;word2:provider2:description2"
     * @return map of word to audio selection
     */
    public static Map<String, AudioSelection> parseAudioSelections(String param) {
        if (param == null || param.trim().isEmpty()) {
            return Map.of();
        }

        Map<String, AudioSelection> selections = new HashMap<>();
        String[] entries = param.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException(
                        "Invalid audio selection format: "
                                + entry
                                + ". Expected format: word:provider:description");
            }
            String word = parts[0].trim();
            String provider = parts[1].trim();
            String description = parts[2].trim();
            selections.put(word, new AudioSelection(provider, description));
        }
        return selections;
    }

    /**
     * Find a pronunciation candidate matching the given selection criteria.
     *
     * @param candidates available candidates
     * @param selection desired provider and description
     * @return matching candidate or null if not found
     */
    public static PronunciationCandidate findMatchingCandidate(
            List<PronunciationCandidate> candidates, AudioSelection selection) {
        for (PronunciationCandidate candidate : candidates) {
            String candidateDesc = stripEmojis(candidate.description());
            if (candidate.label().equals(selection.provider())
                    && candidateDesc.equals(selection.description())) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Remove emoji characters from text for comparison purposes.
     *
     * @param text input text
     * @return text with emojis removed
     */
    public static String stripEmojis(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
    }
}
