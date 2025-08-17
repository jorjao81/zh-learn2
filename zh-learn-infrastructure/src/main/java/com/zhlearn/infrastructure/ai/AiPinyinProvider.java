package com.zhlearn.infrastructure.ai;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.PinyinProvider;
import dev.langchain4j.model.chat.ChatModel;

public class AiPinyinProvider extends BaseAiProvider implements PinyinProvider {
    
    public AiPinyinProvider(AiProviderConfig config, ChatModel chatModel) {
        super(config, chatModel);
    }
    
    @Override
    public Pinyin getPinyin(ChineseWord word) {
        String response = callAiModel(word);
        return parsePinyinFromResponse(response, word);
    }
    
    private Pinyin parsePinyinFromResponse(String response, ChineseWord word) {
        try {
            String[] lines = response.split("\n");
            String romanization = extractValue(lines, "romanization:");
            String toneMarks = extractValue(lines, "tone_marks:");
            
            if (romanization == null) romanization = "unknown";
            if (toneMarks == null) toneMarks = "unknown";
            
            return new Pinyin(romanization, toneMarks);
        } catch (Exception e) {
            return new Pinyin("error-parsing-" + word.characters(), "error");
        }
    }
    
    private String extractValue(String[] lines, String key) {
        for (String line : lines) {
            if (line.toLowerCase().contains(key)) {
                int colonIndex = line.indexOf(':');
                if (colonIndex >= 0 && colonIndex < line.length() - 1) {
                    return line.substring(colonIndex + 1).trim();
                }
            }
        }
        return null;
    }
}