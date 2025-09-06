package com.zhlearn.domain.model;

public class ProviderConfiguration {
    
    private final String defaultProvider;
    private final String pinyinProvider;
    private final String definitionProvider;
    private final String decompositionProvider;
    private final String exampleProvider;
    private final String explanationProvider;
    
    public ProviderConfiguration(String defaultProvider) {
        this(defaultProvider, null, null, null, null, null);
    }
    
    public ProviderConfiguration(
            String defaultProvider,
            String pinyinProvider,
            String definitionProvider, 
            String decompositionProvider,
            String exampleProvider,
            String explanationProvider) {
        this.defaultProvider = defaultProvider != null ? defaultProvider : "dummy";
        this.pinyinProvider = pinyinProvider;
        this.definitionProvider = definitionProvider;
        this.decompositionProvider = decompositionProvider;
        this.exampleProvider = exampleProvider;
        this.explanationProvider = explanationProvider;
    }
    
    public String getPinyinProvider() {
        // Always default to pinyin4j unless explicitly set
        return pinyinProvider != null ? pinyinProvider : "pinyin4j";
    }
    
    public String getDefinitionProvider() {
        return definitionProvider != null ? definitionProvider : defaultProvider;
    }
    
    public String getDecompositionProvider() {
        return decompositionProvider != null ? decompositionProvider : defaultProvider;
    }
    
    public String getExampleProvider() {
        return exampleProvider != null ? exampleProvider : defaultProvider;
    }
    
    public String getExplanationProvider() {
        return explanationProvider != null ? explanationProvider : defaultProvider;
    }
    
    public String getDefaultProvider() {
        return defaultProvider;
    }
    
    @Override
    public String toString() {
        return "ProviderConfiguration{" +
                "default=" + defaultProvider +
                ", pinyin=" + getPinyinProvider() +
                ", definition=" + getDefinitionProvider() +
                ", decomposition=" + getDecompositionProvider() +
                ", example=" + getExampleProvider() +
                ", explanation=" + getExplanationProvider() +
                '}';
    }
}
