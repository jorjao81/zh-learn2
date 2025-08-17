package com.zhlearn.infrastructure.ai;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.provider.ExampleProvider;
import dev.langchain4j.model.chat.ChatModel;

import java.util.ArrayList;
import java.util.List;

public class AiExampleProvider extends BaseAiProvider implements ExampleProvider {
    
    public AiExampleProvider(AiProviderConfig config, ChatModel chatModel) {
        super(config, chatModel);
    }
    
    @Override
    public Example getExamples(ChineseWord word) {
        String response = callAiModel(word);
        return parseExamplesFromResponse(response, word);
    }
    
    private Example parseExamplesFromResponse(String response, ChineseWord word) {
        try {
            List<Example.Usage> usages = new ArrayList<>();
            String[] lines = response.split("\n");
            
            for (String line : lines) {
                if (line.contains("example:")) {
                    String exampleData = line.substring(line.indexOf("example:") + 8).trim();
                    String[] parts = exampleData.split("\\|");
                    
                    if (parts.length >= 2) {
                        String sentence = parts[0].trim();
                        String translation = parts[1].trim();
                        String context = parts.length > 2 ? parts[2].trim() : "general";
                        
                        usages.add(new Example.Usage(sentence, translation, context));
                    }
                }
            }
            
            if (usages.isEmpty()) {
                usages.add(new Example.Usage(
                    "例句包含 " + word.characters() + "。",
                    "Example sentence containing " + word.characters() + ".",
                    "fallback"
                ));
            }
            
            return new Example(usages);
        } catch (Exception e) {
            List<Example.Usage> fallbackUsages = List.of(
                new Example.Usage(
                    "解析错误：" + word.characters(),
                    "Parsing error for: " + word.characters(),
                    "error"
                )
            );
            return new Example(fallbackUsages);
        }
    }
}