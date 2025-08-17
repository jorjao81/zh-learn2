package com.zhlearn.infrastructure.ai;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.provider.ExplanationProvider;
import dev.langchain4j.model.chat.ChatModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AiExplanationProvider extends BaseAiProvider implements ExplanationProvider {
    
    public AiExplanationProvider(AiProviderConfig config, ChatModel chatModel) {
        super(config, chatModel);
    }
    
    @Override
    public Explanation getExplanation(ChineseWord word) {
        String response = callAiModel(word);
        return parseExplanationFromResponse(response, word);
    }
    
    private Explanation parseExplanationFromResponse(String response, ChineseWord word) {
        try {
            String[] lines = response.split("\n");
            String etymology = extractValue(lines, "etymology:");
            String usage = extractValue(lines, "usage:");
            String culturalContext = extractValue(lines, "cultural_context:");
            List<String> similarWords = extractSimilarWords(lines);
            
            if (etymology == null) etymology = "Etymology not available";
            if (usage == null) usage = "Usage information not available";
            if (culturalContext == null) culturalContext = "Cultural context not available";
            if (similarWords.isEmpty()) similarWords = List.of("No similar words available");
            
            return new Explanation(etymology, usage, similarWords, culturalContext);
        } catch (Exception e) {
            return new Explanation(
                "Error parsing etymology for " + word.characters(),
                "Error parsing usage for " + word.characters(),
                List.of("error"),
                "Error parsing cultural context"
            );
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
    
    private List<String> extractSimilarWords(String[] lines) {
        for (String line : lines) {
            if (line.toLowerCase().contains("similar_words:")) {
                int colonIndex = line.indexOf(':');
                if (colonIndex >= 0 && colonIndex < line.length() - 1) {
                    String wordsStr = line.substring(colonIndex + 1).trim();
                    return Arrays.asList(wordsStr.split("[,，]"))
                            .stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                }
            }
        }
        return new ArrayList<>();
    }
}