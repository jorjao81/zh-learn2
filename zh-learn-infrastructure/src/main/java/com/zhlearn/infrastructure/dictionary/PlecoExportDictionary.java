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
import com.zhlearn.infrastructure.pleco.PlecoEntry;
import com.zhlearn.infrastructure.pleco.PlecoExportParser;

/**
 * Dictionary implementation for Pleco export files. Provides lookup capability for words parsed
 * from Pleco export data.
 */
public class PlecoExportDictionary implements Dictionary {

    private static final String DICTIONARY_NAME = "pleco-export";
    private final Map<String, PlecoEntry> wordMap;

    /**
     * Create a dictionary from a Pleco export file.
     *
     * @param parser the parser to use for reading the file
     * @param plecoFilePath path to the Pleco export file
     * @throws IOException if the file cannot be read or parsed
     */
    public PlecoExportDictionary(PlecoExportParser parser, Path plecoFilePath) throws IOException {
        this(parser.parseFile(plecoFilePath));
    }

    /**
     * Create a dictionary from a list of parsed PlecoEntry objects.
     *
     * @param entries the list of PlecoEntry objects
     */
    public PlecoExportDictionary(List<PlecoEntry> entries) {
        this.wordMap =
                entries.stream()
                        .filter(entry -> entry.hanzi() != null && !entry.hanzi().trim().isEmpty())
                        .collect(
                                Collectors.toMap(
                                        entry -> entry.hanzi().trim(),
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

        PlecoEntry entry = wordMap.get(simplifiedCharacters.trim());
        if (entry == null) {
            return Optional.empty();
        }

        return Optional.of(convertToWordAnalysis(entry));
    }

    /**
     * Convert a PlecoEntry to WordAnalysis using direct field mapping. No parsing of internal
     * content - uses fields as-is.
     *
     * @param entry the PlecoEntry to convert
     * @return WordAnalysis object
     */
    private WordAnalysis convertToWordAnalysis(PlecoEntry entry) {
        Hanzi hanzi = new Hanzi(entry.hanzi());
        Pinyin pinyin = new Pinyin(entry.pinyin());

        // Handle empty definitions by using a placeholder
        String defText = entry.definitionText();
        if (defText == null || defText.trim().isEmpty()) {
            defText = "[No definition available in dictionary]";
        }

        Definition definition = new Definition(defText);
        StructuralDecomposition decomposition = new StructuralDecomposition("unknown");
        Example examples = new Example(List.of(), List.of());

        // Use the same placeholder for explanation when definition is empty
        String explText = entry.definitionText();
        if (explText == null || explText.trim().isEmpty()) {
            explText = "[No definition available in dictionary]";
        }
        Explanation explanation = new Explanation(explText);

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
}
