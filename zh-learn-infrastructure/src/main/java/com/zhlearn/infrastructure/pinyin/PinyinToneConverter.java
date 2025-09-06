package com.zhlearn.infrastructure.pinyin;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to convert numbered pinyin (e.g., "shun4") to tone marks (e.g., "shùn").
 *
 * Rules implemented:
 * - Identify syllables as letter blocks optionally ending with tone 1–5.
 * - Neutral tone (5 or 0) removes the number without diacritics.
 * - Vowel selection priority: a > e > ou (mark the 'o') > last vowel (covers iu/ui).
 * - Treat 'u:' and 'v' as 'ü'.
 * - Case-insensitive processing; output is lowercase (common for pinyin display).
 */
public class PinyinToneConverter {

    // Match any pinyin syllable with a trailing tone number (1..5)
    private static final Pattern SYLLABLE_WITH_TONE = Pattern.compile("(?i)([a-züv:]+)([1-5])");

    private static final Map<Character, char[]> DIACRITIC_MAP = new HashMap<>();
    static {
        DIACRITIC_MAP.put('a', new char[]{'ā', 'á', 'ǎ', 'à'});
        DIACRITIC_MAP.put('e', new char[]{'ē', 'é', 'ě', 'è'});
        DIACRITIC_MAP.put('i', new char[]{'ī', 'í', 'ǐ', 'ì'});
        DIACRITIC_MAP.put('o', new char[]{'ō', 'ó', 'ǒ', 'ò'});
        DIACRITIC_MAP.put('u', new char[]{'ū', 'ú', 'ǔ', 'ù'});
        DIACRITIC_MAP.put('ü', new char[]{'ǖ', 'ǘ', 'ǚ', 'ǜ'});
    }

    /**
     * Convert numbered pinyin to tone marks. If no numbered syllables are found,
     * returns the input trimmed.
     */
    public static String convertToToneMarks(String numberedPinyin) {
        if (numberedPinyin == null || numberedPinyin.trim().isEmpty()) {
            return numberedPinyin;
        }

        String input = numberedPinyin.trim();

        Matcher m = SYLLABLE_WITH_TONE.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String base = normalizeUmlaut(m.group(1).toLowerCase());
            int tone = parseTone(m.group(2));
            String replaced = applyTone(base, tone);
            m.appendReplacement(sb, Matcher.quoteReplacement(replaced));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static int parseTone(String toneStr) {
        try {
            return Integer.parseInt(toneStr);
        } catch (NumberFormatException e) {
            return 5; // neutral
        }
    }

    private static String normalizeUmlaut(String s) {
        // Common numbered pinyin notations for ü
        return s.replace("u:", "ü").replace('v', 'ü');
    }

    private static String applyTone(String base, int tone) {
        if (tone <= 0 || tone == 5) {
            // Neutral tone: return without number
            return base;
        }

        int toneIdx = tone - 1; // 1..4 -> 0..3
        int accentIndex = chooseAccentIndex(base);
        if (accentIndex < 0) {
            // No vowel, just return base
            return base;
        }

        char ch = base.charAt(accentIndex);
        char vowel = toPlainVowel(ch);
        char[] diacritics = DIACRITIC_MAP.get(vowel);
        if (diacritics == null) {
            return base; // Shouldn't happen
        }
        char accented = diacritics[toneIdx];
        StringBuilder sb = new StringBuilder(base);
        sb.setCharAt(accentIndex, accented);
        return sb.toString();
    }

    private static int chooseAccentIndex(String s) {
        // Priority: a -> e -> ou(o) -> last vowel
        int idxA = indexOfVowel(s, 'a');
        if (idxA >= 0) return idxA;
        int idxE = indexOfVowel(s, 'e');
        if (idxE >= 0) return idxE;
        int idxOu = indexOfSubstringIgnoreCase(s, "ou");
        if (idxOu >= 0) return idxOu; // mark the 'o' in "ou"
        // Fallback: last vowel among a e i o u ü
        int last = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = toPlainVowel(s.charAt(i));
            if (DIACRITIC_MAP.containsKey(c)) {
                last = i;
            }
        }
        return last;
    }

    private static int indexOfVowel(String s, char vowel) {
        for (int i = 0; i < s.length(); i++) {
            if (toPlainVowel(s.charAt(i)) == vowel) return i;
        }
        return -1;
    }

    private static int indexOfSubstringIgnoreCase(String s, String sub) {
        String lower = s.toLowerCase();
        return lower.indexOf(sub.toLowerCase());
    }

    private static char toPlainVowel(char c) {
        // Normalize any accented vowel back to plain for mapping decisions
        return switch (c) {
            case 'ā', 'á', 'ǎ', 'à', 'a' -> 'a';
            case 'ē', 'é', 'ě', 'è', 'e' -> 'e';
            case 'ī', 'í', 'ǐ', 'ì', 'i' -> 'i';
            case 'ō', 'ó', 'ǒ', 'ò', 'o' -> 'o';
            case 'ū', 'ú', 'ǔ', 'ù', 'u' -> 'u';
            case 'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü' -> 'ü';
            default -> c;
        };
    }
}
