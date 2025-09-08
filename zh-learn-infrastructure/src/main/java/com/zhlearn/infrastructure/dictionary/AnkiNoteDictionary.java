package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.*;
import com.zhlearn.infrastructure.anki.AnkiNote;
import com.zhlearn.infrastructure.anki.AnkiNoteParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnkiNoteDictionary implements Dictionary {
    private static final String DICTIONARY_NAME = "anki-note";
    private final Map<String, AnkiNote> wordMap;

    public AnkiNoteDictionary(AnkiNoteParser parser, Path ankiFilePath) throws IOException {
        this(parser.parseFile(ankiFilePath));
    }

    public AnkiNoteDictionary(List<AnkiNote> notes) {
        this.wordMap = notes.stream()
            .filter(n -> n.simplified() != null && !n.simplified().trim().isEmpty())
            .collect(Collectors.toMap(
                n -> n.simplified().trim(),
                Function.identity(),
                (existing, replacement) -> existing
            ));
    }

    @Override
    public String getName() { return DICTIONARY_NAME; }

    @Override
    public Optional<WordAnalysis> lookup(String simplifiedCharacters) {
        if (simplifiedCharacters == null || simplifiedCharacters.trim().isEmpty()) {
            return Optional.empty();
        }
        AnkiNote note = wordMap.get(simplifiedCharacters.trim());
        if (note == null) return Optional.empty();
        if (note.etymology() == null || note.etymology().trim().isEmpty()) return Optional.empty();
        return Optional.of(convertToWordAnalysis(note));
    }

    private WordAnalysis convertToWordAnalysis(AnkiNote n) {
        Hanzi hanzi = new Hanzi(n.simplified());
        Pinyin pinyin = createPinyin(n);
        Definition definition = createDefinition(n);
        StructuralDecomposition decomposition = createStructuralDecomposition(n);
        Example examples = createExample(n);
        Explanation explanation = createExplanation(n);

        return new WordAnalysis(
            hanzi,
            pinyin,
            definition,
            decomposition,
            examples,
            explanation,
            java.util.Optional.empty(), // no pronunciation available from dictionary
            DICTIONARY_NAME,
            DICTIONARY_NAME + "-pinyin",
            DICTIONARY_NAME + "-definition",
            DICTIONARY_NAME + "-decomposition",
            DICTIONARY_NAME + "-example",
            DICTIONARY_NAME + "-explanation",
            DICTIONARY_NAME + "-audio" // placeholder audio provider
        );
    }

    private Pinyin createPinyin(AnkiNote n) {
        String pinyinText = n.pinyin();
        if (pinyinText == null || pinyinText.trim().isEmpty()) return new Pinyin("unknown");
        return new Pinyin(pinyinText.trim());
    }

    private Definition createDefinition(AnkiNote n) {
        String definitionText = n.definition();
        if (definitionText == null || definitionText.trim().isEmpty()) return new Definition("unknown");
        return new Definition(definitionText.trim().isEmpty() ? "unknown" : definitionText.trim());
    }

    private StructuralDecomposition createStructuralDecomposition(AnkiNote n) {
        String components = n.components();
        if (components == null || components.trim().isEmpty()) return new StructuralDecomposition("unknown");
        return new StructuralDecomposition(components.trim());
    }

    private Example createExample(AnkiNote n) {
        String examplesText = n.examples();
        if (examplesText == null || examplesText.trim().isEmpty()) return new Example(List.of(), List.of());
        Example.Usage usage = new Example.Usage(
            examplesText.trim(),
            "",
            "Example usage",
            DICTIONARY_NAME,
            ""
        );
        return new Example(List.of(usage), List.of());
    }

    private Explanation createExplanation(AnkiNote n) {
        String etymologyText = n.etymology();
        if (etymologyText == null || etymologyText.trim().isEmpty()) return new Explanation("No explanation available");
        return new Explanation(etymologyText.trim());
    }
}
