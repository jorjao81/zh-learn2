package com.zhlearn.application.service;

import com.zhlearn.domain.provider.*;
import com.zhlearn.infrastructure.deepseek.DeepSeekExampleProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekExplanationProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekStructuralDecompositionProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExampleProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExplanationProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoStructuralDecompositionProvider;

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
        try {
            if (configurations.containsKey("DEEPSEEK_API_KEY")) {
                String apiKey = configurations.get("DEEPSEEK_API_KEY");
                String baseUrl = configurations.getOrDefault("DEEPSEEK_BASE_URL", "https://api.deepseek.com");
                
                var deepSeekExplanationProvider = new DeepSeekExplanationProvider(apiKey, baseUrl, "deepseek-chat");
                registerExplanationProvider(deepSeekExplanationProvider);
                
                var deepSeekStructuralDecompositionProvider = new DeepSeekStructuralDecompositionProvider(apiKey, baseUrl, "deepseek-chat");
                registerStructuralDecompositionProvider(deepSeekStructuralDecompositionProvider);
                
                var deepSeekExampleProvider = new DeepSeekExampleProvider(apiKey, baseUrl, "deepseek-chat");
                registerExampleProvider(deepSeekExampleProvider);
            }
        } catch (Exception e) {
            System.err.println("Failed to register DeepSeek providers: " + e.getMessage());
        }
        
        try {
            if (configurations.containsKey("OPENAI_API_KEY")) {
                String apiKey = configurations.get("OPENAI_API_KEY");
                String baseUrl = configurations.getOrDefault("OPENAI_BASE_URL", "https://api.openai.com");
                
                var gpt5NanoExplanationProvider = new GPT5NanoExplanationProvider(apiKey, baseUrl, "gpt-5-nano");
                registerExplanationProvider(gpt5NanoExplanationProvider);
                
                var gpt5NanoStructuralDecompositionProvider = new GPT5NanoStructuralDecompositionProvider(apiKey, baseUrl, "gpt-5-nano");
                registerStructuralDecompositionProvider(gpt5NanoStructuralDecompositionProvider);
                
                var gpt5NanoExampleProvider = new GPT5NanoExampleProvider(apiKey, baseUrl, "gpt-5-nano");
                registerExampleProvider(gpt5NanoExampleProvider);
            }
        } catch (Exception e) {
            System.err.println("Failed to register GPT-5 Nano providers: " + e.getMessage());
        }
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