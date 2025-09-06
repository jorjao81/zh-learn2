package com.zhlearn.infrastructure.pinyin4j;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.PinyinProvider;

public class Pinyin4jProvider implements PinyinProvider {

    private final HanyuPinyinOutputFormat outputFormat;

    public Pinyin4jProvider() {
        outputFormat = new HanyuPinyinOutputFormat();
        outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
        outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
    }

    @Override
    public String getName() {
        return "pinyin4j";
    }

    @Override
    public String getDescription() {
        return "Pure Java pinyin provider using Pinyin4j library";
    }
    
    @Override
    public ProviderType getType() { return ProviderType.LOCAL; }

    @Override
    public Pinyin getPinyin(Hanzi word) {
        String characters = word.characters();
        StringBuilder pinyinBuilder = new StringBuilder();

        for (int i = 0; i < characters.length(); i++) {
            char c = characters.charAt(i);

            if (isCjkIdeograph(c)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, outputFormat);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyinBuilder.append(pinyinArray[0]);
                    } else {
                        pinyinBuilder.append(c);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    pinyinBuilder.append(c);
                }
            } else {
                // Add spacing between CJK and non-CJK boundaries for readability
                if (pinyinBuilder.length() > 0 && i > 0 && isCjkIdeograph(characters.charAt(i - 1))) {
                    pinyinBuilder.append(' ');
                }
                pinyinBuilder.append(c);
                if (i < characters.length() - 1 && isCjkIdeograph(characters.charAt(i + 1))) {
                    pinyinBuilder.append(' ');
                }
            }
        }

        return new Pinyin(pinyinBuilder.toString());
    }

    private boolean isCjkIdeograph(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F
            || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G
            || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT;
    }
}
