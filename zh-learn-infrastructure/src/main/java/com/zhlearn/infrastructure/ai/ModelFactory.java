package com.zhlearn.infrastructure.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.util.Map;

public class ModelFactory {
    
    public static ChatModel createOpenAiModel(String modelName, Map<String, String> config) {
        var builder = OpenAiChatModel.builder()
                .modelName(modelName)
                .temperature(0.7);
        
        if (config.containsKey("api_key")) {
            builder.apiKey(config.get("api_key"));
        }
        if (config.containsKey("base_url")) {
            builder.baseUrl(config.get("base_url"));
        }
        if (config.containsKey("temperature")) {
            builder.temperature(Double.parseDouble(config.get("temperature")));
        }
        if (config.containsKey("max_tokens")) {
            builder.maxTokens(Integer.parseInt(config.get("max_tokens")));
        }
        
        return builder.build();
    }
    
    public static ChatModel createGeminiModel(String modelName, Map<String, String> config) {
        var builder = GoogleAiGeminiChatModel.builder()
                .modelName(modelName)
                .temperature(0.7);
        
        if (config.containsKey("api_key")) {
            builder.apiKey(config.get("api_key"));
        }
        if (config.containsKey("temperature")) {
            builder.temperature(Double.parseDouble(config.get("temperature")));
        }
        if (config.containsKey("max_output_tokens")) {
            builder.maxOutputTokens(Integer.parseInt(config.get("max_output_tokens")));
        }
        
        return builder.build();
    }
    
    public static ChatModel createGenericOpenAiCompatibleModel(String modelName, Map<String, String> config) {
        return createOpenAiModel(modelName, config);
    }
}