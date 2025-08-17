package com.zhlearn.infrastructure.ai;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import dev.langchain4j.model.chat.ChatModel;

import java.util.ArrayList;
import java.util.List;

public class AiStructuralDecompositionProvider extends BaseAiProvider implements StructuralDecompositionProvider {
    
    public AiStructuralDecompositionProvider(AiProviderConfig config, ChatModel chatModel) {
        super(config, chatModel);
    }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(ChineseWord word) {
        String response = callAiModel(word);
        return parseStructuralDecompositionFromResponse(response, word);
    }
    
    private StructuralDecomposition parseStructuralDecompositionFromResponse(String response, ChineseWord word) {
        try {
            List<StructuralDecomposition.Component> components = new ArrayList<>();
            String[] lines = response.split("\n");
            
            for (String line : lines) {
                if (line.contains("component:")) {
                    String componentData = line.substring(line.indexOf("component:") + 10).trim();
                    String[] parts = componentData.split("\\|");
                    
                    if (parts.length >= 2) {
                        String character = parts[0].trim();
                        String meaning = parts[1].trim();
                        String radical = parts.length > 2 ? parts[2].trim() : "unknown";
                        
                        components.add(new StructuralDecomposition.Component(character, meaning, radical));
                    }
                }
            }
            
            if (components.isEmpty()) {
                components.add(new StructuralDecomposition.Component(
                    word.characters(),
                    "No decomposition available",
                    "unknown"
                ));
            }
            
            return new StructuralDecomposition(components);
        } catch (Exception e) {
            List<StructuralDecomposition.Component> fallbackComponents = List.of(
                new StructuralDecomposition.Component(
                    word.characters(),
                    "Error parsing decomposition",
                    "error"
                )
            );
            return new StructuralDecomposition(fallbackComponents);
        }
    }
}