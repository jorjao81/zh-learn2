package com.zhlearn.infrastructure.anki;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.*;

/**
 * Dictionary implementation for Anki export files. Provides lookup capability for words parsed from
 * Anki Chinese 2 note type exports. Used to preserve original field values when improving only
 * selected fields.
 */
public class AnkiNoteDictionary implements Dictionary {

    private static final String DICTIONARY_NAME = "anki-export";
    private final Map<String, AnkiNote> noteMap;

    /**
     * Create a dictionary from a list of parsed AnkiNote objects.
     *
     * @param notes the list of AnkiNote objects from Anki export
     */
    public AnkiNoteDictionary(List<AnkiNote> notes) {
        this.noteMap =
                notes.stream()
                        .filter(
                                note ->
                                        note.simplified() != null
                                                && !note.simplified().trim().isEmpty())
                        .collect(
                                Collectors.toMap(
                                        note -> note.simplified().trim(),
                                        Function.identity(),
                                        (existing, replacement) -> existing // Keep first occurrence
                                        ));
    }

    @Override
    public String getName() {
        return DICTIONARY_NAME;
    }

    @Override
    public Optional<WordAnalysis> lookup(String simplifiedCharacters) {
        if (simplifiedCharacters == null || simplifiedCharacters.trim().isEmpty()) {
            return Optional.empty();
        }

        AnkiNote note = noteMap.get(simplifiedCharacters.trim());
        if (note == null) {
            return Optional.empty();
        }

        return Optional.of(convertToWordAnalysis(note));
    }

    /**
     * Convert an AnkiNote to WordAnalysis using direct field mapping. Preserves original Anki field
     * values for use with Dictionary provider wrappers.
     *
     * @param note the AnkiNote to convert
     * @return WordAnalysis object
     */
    private WordAnalysis convertToWordAnalysis(AnkiNote note) {
        Hanzi hanzi = new Hanzi(note.simplified());
        Pinyin pinyin = new Pinyin(note.pinyin());

        // Use existing definition
        String defText = note.definition();
        if (defText == null || defText.trim().isEmpty()) {
            defText = "[No definition available]";
        }
        Definition definition = new Definition(defText);

        // Use existing components (structural decomposition)
        String compText = note.components();
        if (compText == null || compText.trim().isEmpty()) {
            compText = "unknown";
        }
        StructuralDecomposition decomposition = new StructuralDecomposition(compText);

        // Parse examples from HTML - create minimal Example structure
        Example examples = parseExamplesFromHtml(note.examples());

        // Use existing etymology (explanation)
        String etymText = note.etymology();
        if (etymText == null || etymText.trim().isEmpty()) {
            etymText = "[No explanation available]";
        }
        Explanation explanation = new Explanation(etymText);

        return new WordAnalysis(
                hanzi,
                pinyin,
                definition,
                decomposition,
                examples,
                explanation,
                Optional.empty() // Audio handled separately in ImproveAnkiCommand
                );
    }

    /**
     * Parse examples from Anki HTML format. Creates a minimal Example structure that satisfies
     * domain requirements. The original HTML will be preserved during export if examples are not
     * being improved.
     *
     * @param html the HTML examples field from Anki
     * @return Example object, possibly empty
     */
    private Example parseExamplesFromHtml(String html) {
        if (html == null || html.isBlank()) {
            return new Example(List.of(), List.of());
        }

        // Extract first meaningful text as a placeholder sentence
        // This is a simple extraction - the real HTML is preserved during export
        String sentence = extractFirstSentence(html);
        if (sentence.isEmpty()) {
            return new Example(List.of(), List.of());
        }

        // Create minimal Usage to satisfy Example requirements
        Example.Usage usage =
                new Example.Usage(
                        sentence,
                        "", // pinyin
                        "translation", // translation required by domain
                        "", // context
                        "" // breakdown
                        );

        return new Example(List.of(usage), List.of());
    }

    /**
     * Extract first sentence-like content from HTML by removing tags and taking first meaningful
     * text.
     */
    private String extractFirstSentence(String html) {
        if (html == null) {
            return "";
        }

        // Remove HTML tags
        String text = html.replaceAll("<[^>]+>", " ");
        // Collapse whitespace
        text = text.replaceAll("\\s+", " ").trim();

        // Take first 50 characters or up to first period/newline
        if (text.length() > 50) {
            int period = text.indexOf('.');
            if (period > 0 && period < 50) {
                return text.substring(0, period + 1).trim();
            }
            return text.substring(0, 50).trim() + "...";
        }

        return text;
    }
}
