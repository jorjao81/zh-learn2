package com.zhlearn.infrastructure.dictionary;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.*;
import com.zhlearn.infrastructure.anki.AnkiCard;
import com.zhlearn.infrastructure.anki.AnkiCardParser;

public class AnkiCardDictionary implements Dictionary {
    private static final String DICTIONARY_NAME = "anki-card";
    private final Map<String, AnkiCard> wordMap;

    public AnkiCardDictionary(AnkiCardParser parser, Path ankiFilePath) throws IOException {
        this(parser.parseFile(ankiFilePath));
    }

    public AnkiCardDictionary(List<AnkiCard> ankiCards) {
        this.wordMap =
                ankiCards.stream()
                        .filter(
                                card ->
                                        card.simplified() != null
                                                && !card.simplified().trim().isEmpty())
                        .collect(
                                Collectors.toMap(
                                        card -> card.simplified().trim(),
                                        Function.identity(),
                                        (existing, replacement) ->
                                                existing // Keep first occurrence if duplicates
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

        AnkiCard card = wordMap.get(simplifiedCharacters.trim());
        if (card == null) {
            return Optional.empty();
        }

        // Return empty if critical fields are missing
        if (card.etymology() == null || card.etymology().trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertToWordAnalysis(card));
    }

    private WordAnalysis convertToWordAnalysis(AnkiCard card) {
        Hanzi hanzi = new Hanzi(card.simplified());
        Pinyin pinyin = createPinyin(card);
        Definition definition = createDefinition(card);
        StructuralDecomposition decomposition = createStructuralDecomposition(card);
        Example examples = createExample(card);
        Explanation explanation = createExplanation(card);

        return new WordAnalysis(
                hanzi,
                pinyin,
                definition,
                decomposition,
                examples,
                explanation,
                Optional.empty() // no pronunciation available from dictionary
                );
    }

    private Pinyin createPinyin(AnkiCard card) {
        String pinyinText = card.pinyin();
        if (pinyinText == null || pinyinText.trim().isEmpty()) {
            return new Pinyin("unknown");
        }
        return new Pinyin(pinyinText.trim());
    }

    private Definition createDefinition(AnkiCard card) {
        String definitionText = card.definition();
        if (definitionText == null || definitionText.trim().isEmpty()) {
            return new Definition("unknown");
        }

        return new Definition(definitionText.trim().isEmpty() ? "unknown" : definitionText.trim());
    }

    private StructuralDecomposition createStructuralDecomposition(AnkiCard card) {
        String components = card.components();
        if (components == null || components.trim().isEmpty()) {
            return new StructuralDecomposition("unknown");
        }
        return new StructuralDecomposition(components.trim());
    }

    private Example createExample(AnkiCard card) {
        String examplesText = card.examples();
        if (examplesText == null || examplesText.trim().isEmpty()) {
            return new Example(List.of(), List.of());
        }

        // Simple parsing - create one usage from the examples field
        Example.Usage usage =
                new Example.Usage(
                        examplesText.trim(),
                        "", // No pinyin in AnkiCard examples
                        "Example usage", // Generic translation
                        "anki-card", // Context
                        "" // No breakdown available in AnkiCard
                        );

        return new Example(List.of(usage), List.of());
    }

    private Explanation createExplanation(AnkiCard card) {
        String etymologyText = card.etymology();
        if (etymologyText == null || etymologyText.trim().isEmpty()) {
            return new Explanation("No explanation available");
        }
        return new Explanation(etymologyText.trim());
    }
}
