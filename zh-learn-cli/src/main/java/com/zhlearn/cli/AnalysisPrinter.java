package com.zhlearn.cli;

import com.zhlearn.application.format.ExamplesHtmlFormatter;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.WordAnalysis;

/**
 * Shared printing utility for WordAnalysis in CLI. Used by both the single-word and batch commands
 * to ensure identical output.
 */
public class AnalysisPrinter {
    private final ExamplesHtmlFormatter examplesHtmlFormatter;
    private final TerminalFormatter terminalFormatter;

    public AnalysisPrinter(
            ExamplesHtmlFormatter examplesHtmlFormatter, TerminalFormatter terminalFormatter) {
        this.examplesHtmlFormatter = examplesHtmlFormatter;
        this.terminalFormatter = terminalFormatter;
    }

    public void printFormatted(WordAnalysis analysis) {
        int width = terminalFormatter.getTerminalWidth();

        String wordContent =
                terminalFormatter.formatChineseWord(
                        analysis.word().characters(), analysis.pinyin().pinyin());
        System.out.println(terminalFormatter.createBox("Chinese Word", wordContent, width));
        System.out.println();

        String pinyinContent =
                terminalFormatter.formatChineseWord("拼音", analysis.pinyin().pinyin());
        System.out.println(terminalFormatter.createBox("Pinyin", pinyinContent, width));
        System.out.println();

        String defContent = terminalFormatter.formatDefinition(analysis.definition().meaning());
        System.out.println(terminalFormatter.createBox("Definition", defContent, width));
        System.out.println();

        String decompositionContent =
                terminalFormatter.formatStructuralDecomposition(
                        analysis.structuralDecomposition().decomposition());
        System.out.println(
                terminalFormatter.createBox(
                        "Structural Decomposition", decompositionContent, width));
        System.out.println();

        String examplesHtml = examplesHtmlFormatter.format(analysis.examples());
        String formattedExamples = terminalFormatter.convertHtmlToAnsi(examplesHtml);
        System.out.println(terminalFormatter.createBox("Examples", formattedExamples, width));
        System.out.println();

        String explanationContent =
                terminalFormatter.convertHtmlToAnsi(analysis.explanation().explanation());
        System.out.println(terminalFormatter.createBox("Explanation", explanationContent, width));

        Runtime.getRuntime().addShutdownHook(new Thread(TerminalFormatter::shutdown));
    }

    public void printRaw(WordAnalysis analysis) {
        System.out.println("Chinese Word: " + analysis.word().characters());
        System.out.println();

        System.out.println("Pinyin: " + analysis.pinyin().pinyin());
        System.out.println();

        System.out.println("Definition: " + analysis.definition().meaning());
        System.out.println();

        System.out.println(
                "Structural Decomposition: " + analysis.structuralDecomposition().decomposition());
        System.out.println();

        System.out.println("Examples:");
        for (Example.Usage usage : analysis.examples().usages()) {
            System.out.println("  Chinese: " + usage.sentence());
            System.out.println("  Pinyin: " + usage.pinyin());
            System.out.println("  English: " + usage.translation());
            if (usage.context() != null && !usage.context().isEmpty()) {
                System.out.println("  Context: " + usage.context());
            }
        }
        // No standalone sentences section
        if (analysis.examples().phoneticSeries() != null
                && !analysis.examples().phoneticSeries().isEmpty()) {
            System.out.println("  Phonetic series:");
            for (Example.SeriesItem item : analysis.examples().phoneticSeries()) {
                String pinyin = item.pinyin() == null ? "" : (" " + item.pinyin());
                String meaning =
                        item.meaning() == null || item.meaning().isBlank()
                                ? ""
                                : (" " + item.meaning());
                System.out.println("    • " + item.hanzi() + pinyin + meaning);
            }
        }
        System.out.println();

        System.out.println("Explanation: " + analysis.explanation().explanation());
    }
}
