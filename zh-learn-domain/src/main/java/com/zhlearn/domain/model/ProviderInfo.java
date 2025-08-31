package com.zhlearn.domain.model;

import java.util.Set;

public record ProviderInfo(
    String name,
    String description,
    ProviderType type,
    Set<ProviderClass> supportedClasses
) {
    
    public enum ProviderType {
        AI("AI-powered providers"),
        DICTIONARY("Dictionary-based providers"), 
        DUMMY("Test/dummy providers");
        
        private final String description;
        
        ProviderType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum ProviderClass {
        PINYIN("Pinyin generation"),
        DEFINITION("Word definitions"),
        STRUCTURAL_DECOMPOSITION("Character structure analysis"),
        EXAMPLE("Usage examples"),
        EXPLANATION("Detailed explanations");
        
        private final String description;
        
        ProviderClass(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}