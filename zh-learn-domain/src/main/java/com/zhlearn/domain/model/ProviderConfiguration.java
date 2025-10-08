package com.zhlearn.domain.model;

public class ProviderConfiguration {

    private final String defaultProvider;
    private final String pinyinProvider;
    private final String definitionProvider;
    private final String definitionFormatterProvider;
    private final String decompositionProvider;
    private final String exampleProvider;
    private final String explanationProvider;
    private final String audioProvider;

    public ProviderConfiguration(String defaultProvider) {
        this(defaultProvider, null, null, null, null, null, null, null);
    }

    public ProviderConfiguration(
            String defaultProvider,
            String pinyinProvider,
            String definitionProvider,
            String definitionFormatterProvider,
            String decompositionProvider,
            String exampleProvider,
            String explanationProvider,
            String audioProvider) {
        this.defaultProvider = defaultProvider != null ? defaultProvider : "dummy";
        this.pinyinProvider = pinyinProvider;
        this.definitionProvider = definitionProvider;
        this.definitionFormatterProvider = definitionFormatterProvider;
        this.decompositionProvider = decompositionProvider;
        this.exampleProvider = exampleProvider;
        this.explanationProvider = explanationProvider;
        this.audioProvider = audioProvider;
    }

    public String getPinyinProvider() {
        // Always default to pinyin4j unless explicitly set
        return pinyinProvider != null ? pinyinProvider : "pinyin4j";
    }

    public String getDefinitionProvider() {
        return definitionProvider != null ? definitionProvider : defaultProvider;
    }

    public String getDefinitionFormatterProvider() {
        return definitionFormatterProvider != null ? definitionFormatterProvider : defaultProvider;
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

    public String getAudioProvider() {
        return audioProvider != null ? audioProvider : defaultProvider;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    @Override
    public String toString() {
        return "ProviderConfiguration{"
                + "default="
                + defaultProvider
                + ", pinyin="
                + getPinyinProvider()
                + ", definition="
                + getDefinitionProvider()
                + ", definitionFormatter="
                + getDefinitionFormatterProvider()
                + ", decomposition="
                + getDecompositionProvider()
                + ", example="
                + getExampleProvider()
                + ", explanation="
                + getExplanationProvider()
                + ", audio="
                + getAudioProvider()
                + '}';
    }
}
