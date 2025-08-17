package com.zhlearn.infrastructure.ai;

import com.zhlearn.domain.model.ChineseWord;
import dev.langchain4j.model.chat.ChatModel;

public abstract class BaseAiProvider {
    protected final AiProviderConfig config;
    protected final ChatModel chatModel;
    
    protected BaseAiProvider(AiProviderConfig config, ChatModel chatModel) {
        this.config = config;
        this.chatModel = chatModel;
    }
    
    public String getName() {
        return config.name();
    }
    
    protected String buildPrompt(ChineseWord word) {
        var promptBuilder = new StringBuilder();
        promptBuilder.append(config.prompt()).append("\n\n");
        
        if (!config.examples().isEmpty()) {
            promptBuilder.append("Examples:\n");
            for (String example : config.examples()) {
                promptBuilder.append("- ").append(example).append("\n");
            }
            promptBuilder.append("\n");
        }
        
        promptBuilder.append("Chinese word: ").append(word.characters());
        
        return promptBuilder.toString();
    }
    
    protected String callAiModel(ChineseWord word) {
        String prompt = buildPrompt(word);
        return chatModel.chat(prompt);
    }
}