package com.zhlearn.cli;

import com.zhlearn.domain.model.WordAnalysis;

/**
 * Shared printing utility for WordAnalysis in CLI.
 * Used by both the single-word and batch commands to ensure identical output.
 */
public final class AnalysisPrinter {
    private AnalysisPrinter() {}

    public static void printFormatted(WordAnalysis analysis) {
        int width = TerminalFormatter.getTerminalWidth();

        String wordContent = TerminalFormatter.formatChineseWord(
                analysis.word().characters(), analysis.pinyin().pinyin()) + "\n" +
                TerminalFormatter.formatProvider("Default: " + analysis.providerName());
        System.out.println(TerminalFormatter.createBox("Chinese Word", wordContent, width));
        System.out.println();

        String pinyinContent = TerminalFormatter.formatChineseWord("拼音", analysis.pinyin().pinyin()) + "\n" +
                TerminalFormatter.formatProvider(analysis.pinyinProvider());
        System.out.println(TerminalFormatter.createBox("Pinyin", pinyinContent, width));
        System.out.println();

        String defContent = TerminalFormatter.formatDefinition(
                analysis.definition().meaning(), analysis.definition().partOfSpeech()) + "\n" +
                TerminalFormatter.formatProvider(analysis.definitionProvider());
        System.out.println(TerminalFormatter.createBox("Definition", defContent, width));
        System.out.println();

        String decompositionContent = TerminalFormatter.formatStructuralDecomposition(
                analysis.structuralDecomposition().decomposition()) + "\n" +
                TerminalFormatter.formatProvider(analysis.decompositionProvider());
        System.out.println(TerminalFormatter.createBox("Structural Decomposition", decompositionContent, width));
        System.out.println();

        String groupedExamples = TerminalFormatter.formatGroupedExamples(analysis.examples().usages());
        String exampleContent = groupedExamples + "\n" + TerminalFormatter.formatProvider(analysis.exampleProvider());
        System.out.println(TerminalFormatter.createBox("Examples", exampleContent, width));
        System.out.println();

        String explanationContent = TerminalFormatter.convertHtmlToAnsi(
                analysis.explanation().explanation()) + "\n" +
                TerminalFormatter.formatProvider(analysis.explanationProvider());
        System.out.println(TerminalFormatter.createBox("Explanation", explanationContent, width));

        Runtime.getRuntime().addShutdownHook(new Thread(TerminalFormatter::shutdown));
    }

    public static void printRaw(WordAnalysis analysis) {
        System.out.println("Chinese Word: " + analysis.word().characters());
        System.out.println("Default Provider: " + analysis.providerName());
        System.out.println();

        System.out.println("Pinyin: " + analysis.pinyin().pinyin());
        System.out.println("  Provider: " + analysis.pinyinProvider());
        System.out.println();

        System.out.println("Definition: " + analysis.definition().meaning());
        System.out.println("Part of Speech: " + analysis.definition().partOfSpeech());
        System.out.println("  Provider: " + analysis.definitionProvider());
        System.out.println();

        System.out.println("Structural Decomposition: " + analysis.structuralDecomposition().decomposition());
        System.out.println("  Provider: " + analysis.decompositionProvider());
        System.out.println();

        System.out.println("Examples:");
        for (var usage : analysis.examples().usages()) {
            System.out.println("  Chinese: " + usage.sentence());
            System.out.println("  Pinyin: " + usage.pinyin());
            System.out.println("  English: " + usage.translation());
            if (usage.context() != null && !usage.context().isEmpty()) {
                System.out.println("  Context: " + usage.context());
            }
        }
        System.out.println("  Provider: " + analysis.exampleProvider());
        System.out.println();

        System.out.println("Explanation: " + analysis.explanation().explanation());
        System.out.println("  Provider: " + analysis.explanationProvider());
    }
}

