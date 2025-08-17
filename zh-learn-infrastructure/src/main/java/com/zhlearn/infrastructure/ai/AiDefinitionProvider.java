package com.zhlearn.infrastructure.ai;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.provider.DefinitionProvider;
import dev.langchain4j.model.chat.ChatModel;

public class AiDefinitionProvider extends BaseAiProvider implements DefinitionProvider {
    
    public AiDefinitionProvider(AiProviderConfig config, ChatModel chatModel) {
        super(config, chatModel);
    }
    
    @Override
    public Definition getDefinition(ChineseWord word) {
        String response = callAiModel(word);
        return parseDefinitionFromResponse(response, word);
    }
    
    private Definition parseDefinitionFromResponse(String response, ChineseWord word) {
        try {
            String[] lines = response.split("\n");
            String meaning = extractValue(lines, "meaning:");
            String partOfSpeech = extractValue(lines, "part_of_speech:");
            
            if (meaning == null) meaning = "No definition available";
            if (partOfSpeech == null) partOfSpeech = "unknown";
            
            return new Definition(meaning, partOfSpeech);
        } catch (Exception e) {
            return new Definition("Error parsing definition for " + word.characters(), "unknown");
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