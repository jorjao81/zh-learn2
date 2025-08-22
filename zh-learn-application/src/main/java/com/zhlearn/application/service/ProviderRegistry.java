package com.zhlearn.application.service;

import com.zhlearn.domain.provider.*;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderRegistry {
    
    private final Map<String, PinyinProvider> pinyinProviders = new ConcurrentHashMap<>();
    private final Map<String, DefinitionProvider> definitionProviders = new ConcurrentHashMap<>();
    private final Map<String, StructuralDecompositionProvider> decompositionProviders = new ConcurrentHashMap<>();
    private final Map<String, ExampleProvider> exampleProviders = new ConcurrentHashMap<>();
    private final Map<String, ExplanationProvider> explanationProviders = new ConcurrentHashMap<>();
    
    private final Map<String, String> configurations = new ConcurrentHashMap<>();
    
    public ProviderRegistry() {
        loadConfiguration();
        registerDefaultProviders();
    }
    
    private void loadConfiguration() {
        configurations.putAll(System.getenv());
        
        Properties props = System.getProperties();
        for (String name : props.stringPropertyNames()) {
            configurations.put(name, props.getProperty(name));
        }
    }
    
    private void registerDefaultProviders() {
        registerDummyProviders();
        // TODO: Re-enable AI providers when module system is fixed
        // registerAvailableAiProviders();
    }
    
    private void registerDummyProviders() {

    }
    
    private void registerAvailableAiProviders() {
        String[] aiModels = {
            "gpt-4o", "gpt-4o-mini", "gpt-5", "gpt-5-nano",
            "gemini-2.0-flash-exp", "gemini-2.5-pro", "gemini-2.5-flash",
            "qwen-turbo", "qwen-plus", "qwen-max",
            "deepseek-chat", "deepseek-coder",
            "grok-4", "grok-4-turbo"
        };
        
        for (String model : aiModels) {
            if (isModelConfigured(model)) {
                try {
                    registerAiProvidersForModel(model);
                } catch (Exception e) {
                    System.err.println("Failed to register AI provider " + model + ": " + e.getMessage());
                }
            }
        }
    }
    
    private boolean isModelConfigured(String model) {
        if (model.startsWith("gpt")) {
            return configurations.containsKey("OPENAI_API_KEY");
        } else if (model.startsWith("gemini")) {
            return configurations.containsKey("GEMINI_API_KEY");
        } else if (model.startsWith("qwen")) {
            return configurations.containsKey("QWEN_API_KEY");
        } else if (model.startsWith("deepseek")) {
            return configurations.containsKey("DEEPSEEK_API_KEY");
        } else if (model.startsWith("grok")) {
            return configurations.containsKey("GROK_API_KEY");
        }
        return false;
    }
    
    private void registerAiProvidersForModel(String model) {
        Map<String, String> config = buildConfigForModel(model);
        
        String pinyinProviderName = model + "-pinyin";
        String definitionProviderName = model + "-definition";
        String decompositionProviderName = model + "-structural-decomposition";
        String exampleProviderName = model + "-example";
        String explanationProviderName = model + "-explanation";
        
        // TODO: Re-enable when AiProviderFactory is available
        // registerPinyinProvider(AiProviderFactory.createPinyinProvider(pinyinProviderName, config));
        // registerDefinitionProvider(AiProviderFactory.createDefinitionProvider(definitionProviderName, config));
        // registerStructuralDecompositionProvider(AiProviderFactory.createStructuralDecompositionProvider(decompositionProviderName, config));
        // registerExampleProvider(AiProviderFactory.createExampleProvider(exampleProviderName, config));
        // registerExplanationProvider(AiProviderFactory.createExplanationProvider(explanationProviderName, config));
    }
    
    private Map<String, String> buildConfigForModel(String model) {
        Map<String, String> config = new HashMap<>();
        
        if (model.startsWith("gpt")) {
            config.put("api_key", configurations.get("OPENAI_API_KEY"));
            if (configurations.containsKey("OPENAI_BASE_URL")) {
                config.put("base_url", configurations.get("OPENAI_BASE_URL"));
            }
        } else if (model.startsWith("gemini")) {
            config.put("api_key", configurations.get("GEMINI_API_KEY"));
        } else if (model.startsWith("qwen")) {
            config.put("api_key", configurations.get("QWEN_API_KEY"));
            config.put("base_url", configurations.getOrDefault("QWEN_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1"));
        } else if (model.startsWith("deepseek")) {
            config.put("api_key", configurations.get("DEEPSEEK_API_KEY"));
            config.put("base_url", configurations.getOrDefault("DEEPSEEK_BASE_URL", "https://api.deepseek.com"));
        } else if (model.startsWith("grok")) {
            config.put("api_key", configurations.get("GROK_API_KEY"));
            config.put("base_url", configurations.getOrDefault("GROK_BASE_URL", "https://api.x.ai/v1"));
        }
        
        return config;
    }
    
    public void registerPinyinProvider(PinyinProvider provider) {
        pinyinProviders.put(provider.getName(), provider);
    }
    
    public void registerDefinitionProvider(DefinitionProvider provider) {
        definitionProviders.put(provider.getName(), provider);
    }
    
    public void registerStructuralDecompositionProvider(StructuralDecompositionProvider provider) {
        decompositionProviders.put(provider.getName(), provider);
    }
    
    public void registerExampleProvider(ExampleProvider provider) {
        exampleProviders.put(provider.getName(), provider);
    }
    
    public void registerExplanationProvider(ExplanationProvider provider) {
        explanationProviders.put(provider.getName(), provider);
    }
    
    public Optional<PinyinProvider> getPinyinProvider(String name) {
        System.out.println(pinyinProviders);
        return Optional.ofNullable(pinyinProviders.get(name));
    }
    
    public Optional<DefinitionProvider> getDefinitionProvider(String name) {
        return Optional.ofNullable(definitionProviders.get(name));
    }
    
    public Optional<StructuralDecompositionProvider> getStructuralDecompositionProvider(String name) {
        return Optional.ofNullable(decompositionProviders.get(name));
    }
    
    public Optional<ExampleProvider> getExampleProvider(String name) {
        return Optional.ofNullable(exampleProviders.get(name));
    }
    
    public Optional<ExplanationProvider> getExplanationProvider(String name) {
        return Optional.ofNullable(explanationProviders.get(name));
    }
    
    public Set<String> getAvailablePinyinProviders() {
        return new HashSet<>(pinyinProviders.keySet());
    }
    
    public Set<String> getAvailableDefinitionProviders() {
        return new HashSet<>(definitionProviders.keySet());
    }
    
    public Set<String> getAvailableStructuralDecompositionProviders() {
        return new HashSet<>(decompositionProviders.keySet());
    }
    
    public Set<String> getAvailableExampleProviders() {
        return new HashSet<>(exampleProviders.keySet());
    }
    
    public Set<String> getAvailableExplanationProviders() {
        return new HashSet<>(explanationProviders.keySet());
    }
}