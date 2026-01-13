package com.zhlearn.domain.model;

/**
 * Represents a single grammar sentence exercise with Chinese, Pinyin, and English. The sentences
 * contain highlight markers (==text==) indicating the grammar point.
 */
public record GrammarSentenceExercise(String sentenceCN, String sentencePinyin, String sentenceEN) {

    public GrammarSentenceExercise {
        if (sentenceCN == null || sentenceCN.isBlank()) {
            throw new IllegalArgumentException("Chinese sentence cannot be null or blank");
        }
        if (sentencePinyin == null || sentencePinyin.isBlank()) {
            throw new IllegalArgumentException("Pinyin cannot be null or blank");
        }
        if (sentenceEN == null || sentenceEN.isBlank()) {
            throw new IllegalArgumentException("English sentence cannot be null or blank");
        }
        if (!containsHighlight(sentenceCN)) {
            throw new IllegalArgumentException(
                    "Chinese sentence must contain highlight markers (==text==): " + sentenceCN);
        }
        if (!containsHighlight(sentenceEN)) {
            throw new IllegalArgumentException(
                    "English sentence must contain highlight markers (==text==): " + sentenceEN);
        }
    }

    private static boolean containsHighlight(String text) {
        return text.contains("==") && text.indexOf("==") != text.lastIndexOf("==");
    }

    /** Extract the highlighted text from the Chinese sentence. */
    public String extractHighlightCN() {
        return extractHighlight(sentenceCN);
    }

    /** Extract the highlighted text from the English sentence. */
    public String extractHighlightEN() {
        return extractHighlight(sentenceEN);
    }

    private static String extractHighlight(String text) {
        int start = text.indexOf("==");
        if (start == -1) return "";
        int end = text.indexOf("==", start + 2);
        if (end == -1) return "";
        return text.substring(start + 2, end);
    }

    /** Convert Chinese sentence to HTML with span highlight. */
    public String sentenceCNAsHtml() {
        return convertHighlightToHtml(sentenceCN);
    }

    /** Convert English sentence to HTML with span highlight. */
    public String sentenceENAsHtml() {
        return convertHighlightToHtml(sentenceEN);
    }

    private static String convertHighlightToHtml(String text) {
        return text.replaceAll("==([^=]+)==", "<span class=\"hl\">$1</span>");
    }
}
