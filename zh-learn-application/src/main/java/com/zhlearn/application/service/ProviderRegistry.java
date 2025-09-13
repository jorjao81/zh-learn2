package com.zhlearn.application.service;

import com.zhlearn.domain.model.ProviderInfo;
import com.zhlearn.domain.model.ProviderInfo.ProviderClass;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ProviderRegistry {
    
    private final Map<String, PinyinProvider> pinyinProviders = new ConcurrentHashMap<>();
    private final Map<String, DefinitionProvider> definitionProviders = new ConcurrentHashMap<>();
    private final Map<String, StructuralDecompositionProvider> decompositionProviders = new ConcurrentHashMap<>();
    private final Map<String, ExampleProvider> exampleProviders = new ConcurrentHashMap<>();
    private final Map<String, ExplanationProvider> explanationProviders = new ConcurrentHashMap<>();
    private final Map<String, AudioProvider> audioProviders = new ConcurrentHashMap<>();
    
    private final Map<String, String> configurations = new ConcurrentHashMap<>();
    
    public ProviderRegistry() {
        loadConfiguration();
        discoverAndRegisterProviders();
    }
    
    private void loadConfiguration() {
        configurations.putAll(System.getenv());
        
        Properties props = System.getProperties();
        for (String name : props.stringPropertyNames()) {
            configurations.put(name, props.getProperty(name));
        }
    }
    
    private void discoverAndRegisterProviders() {
        // Discover via ServiceLoader; providers are implemented in infrastructure module
        loadViaServiceLoader(PinyinProvider.class, this::registerPinyinProvider);
        loadViaServiceLoader(DefinitionProvider.class, this::registerDefinitionProvider);
        loadViaServiceLoader(StructuralDecompositionProvider.class, this::registerStructuralDecompositionProvider);
        loadViaServiceLoader(ExampleProvider.class, this::registerExampleProvider);
        loadViaServiceLoader(ExplanationProvider.class, this::registerExplanationProvider);
        loadViaServiceLoader(AudioProvider.class, this::registerAudioProvider);
    }

    private <T> void loadViaServiceLoader(Class<T> serviceType, Consumer<T> registrar) {
        int count = 0;
        try {
            for (T provider : ServiceLoader.load(serviceType)) {
                registrar.accept(provider);
                count++;
            }
        } catch (Throwable ignored) {
            // Fall through to TCCL attempt
        }
        if (count == 0) {
            try {
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                for (T provider : ServiceLoader.load(serviceType, tccl)) {
                    registrar.accept(provider);
                    count++;
                }
            } catch (Throwable ignoredAlso) {
                // ignore
            }
        }
    }

    // No reflective fallback; clean JPMS ServiceLoader only
    
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
    
    public void registerAudioProvider(AudioProvider provider) {
        audioProviders.put(provider.getName(), provider);
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
    
    public Optional<AudioProvider> getAudioProvider(String name) {
        return Optional.ofNullable(audioProviders.get(name));
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
    
    public Set<String> getAvailableAudioProviders() {
        return new HashSet<>(audioProviders.keySet());
    }
    
    public List<ProviderInfo> getAllProviderInfo() {
        Map<String, ProviderInfo> providerInfoMap = new HashMap<>();
        
        // Collect all providers with their supported classes
        pinyinProviders.forEach((name, provider) -> {
            ProviderInfo baseInfo = createBaseProviderInfo(name, provider.getDescription(), provider.getType());
            ProviderInfo infoWithPinyin = new ProviderInfo(
                baseInfo.name(),
                baseInfo.description(),
                baseInfo.type(),
                Set.of(ProviderClass.PINYIN)
            );
            providerInfoMap.merge(name, infoWithPinyin,
                (existing, newInfo) -> new ProviderInfo(
                    existing.name(),
                    existing.description(),
                    existing.type(),
                    addToSet(existing.supportedClasses(), ProviderClass.PINYIN)
                ));
        });
        
        definitionProviders.forEach((name, provider) -> {
            ProviderInfo baseInfo = createBaseProviderInfo(name, provider.getDescription(), provider.getType());
            ProviderInfo infoWithDefinition = new ProviderInfo(
                baseInfo.name(),
                baseInfo.description(),
                baseInfo.type(),
                Set.of(ProviderClass.DEFINITION)
            );
            providerInfoMap.merge(name, infoWithDefinition,
                (existing, newInfo) -> new ProviderInfo(
                    existing.name(),
                    existing.description(),
                    existing.type(),
                    addToSet(existing.supportedClasses(), ProviderClass.DEFINITION)
                ));
        });
        
        decompositionProviders.forEach((name, provider) -> {
            ProviderInfo baseInfo = createBaseProviderInfo(name, provider.getDescription(), provider.getType());
            ProviderInfo infoWithDecomposition = new ProviderInfo(
                baseInfo.name(),
                baseInfo.description(),
                baseInfo.type(),
                Set.of(ProviderClass.STRUCTURAL_DECOMPOSITION)
            );
            providerInfoMap.merge(name, infoWithDecomposition,
                (existing, newInfo) -> new ProviderInfo(
                    existing.name(),
                    existing.description(),
                    existing.type(),
                    addToSet(existing.supportedClasses(), ProviderClass.STRUCTURAL_DECOMPOSITION)
                ));
        });
        
        exampleProviders.forEach((name, provider) -> {
            ProviderInfo baseInfo = createBaseProviderInfo(name, provider.getDescription(), provider.getType());
            ProviderInfo infoWithExample = new ProviderInfo(
                baseInfo.name(),
                baseInfo.description(),
                baseInfo.type(),
                Set.of(ProviderClass.EXAMPLE)
            );
            providerInfoMap.merge(name, infoWithExample,
                (existing, newInfo) -> new ProviderInfo(
                    existing.name(),
                    existing.description(),
                    existing.type(),
                    addToSet(existing.supportedClasses(), ProviderClass.EXAMPLE)
                ));
        });
        
        explanationProviders.forEach((name, provider) -> {
            ProviderInfo baseInfo = createBaseProviderInfo(name, provider.getDescription(), provider.getType());
            ProviderInfo infoWithExplanation = new ProviderInfo(
                baseInfo.name(),
                baseInfo.description(),
                baseInfo.type(),
                Set.of(ProviderClass.EXPLANATION)
            );
            providerInfoMap.merge(name, infoWithExplanation,
                (existing, newInfo) -> new ProviderInfo(
                    existing.name(),
                    existing.description(),
                    existing.type(),
                    addToSet(existing.supportedClasses(), ProviderClass.EXPLANATION)
                ));
        });
        
        audioProviders.forEach((name, provider) -> {
            ProviderInfo baseInfo = createBaseProviderInfo(name, provider.getDescription(), provider.getType());
            ProviderInfo infoWithAudio = new ProviderInfo(
                baseInfo.name(),
                baseInfo.description(),
                baseInfo.type(),
                Set.of(ProviderClass.AUDIO)
            );
            providerInfoMap.merge(name, infoWithAudio,
                (existing, newInfo) -> new ProviderInfo(
                    existing.name(),
                    existing.description(),
                    existing.type(),
                    addToSet(existing.supportedClasses(), ProviderClass.AUDIO)
                ));
        });
        
        return new ArrayList<>(providerInfoMap.values());
    }
    
    public List<String> findSimilarProviders(String requestedProvider) {
        Set<String> allProviders = new HashSet<>();
        allProviders.addAll(pinyinProviders.keySet());
        allProviders.addAll(definitionProviders.keySet());
        allProviders.addAll(decompositionProviders.keySet());
        allProviders.addAll(exampleProviders.keySet());
        allProviders.addAll(explanationProviders.keySet());
        
        String lowerRequested = requestedProvider.toLowerCase();
        
        return allProviders.stream()
            .filter(provider -> isSimilar(lowerRequested, provider.toLowerCase()))
            .sorted((a, b) -> Integer.compare(
                calculateSimilarityScore(lowerRequested, a.toLowerCase()),
                calculateSimilarityScore(lowerRequested, b.toLowerCase())
            ))
            .limit(5)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    private ProviderInfo createBaseProviderInfo(String name, String description, ProviderType type) {
        return new ProviderInfo(name, description, type, new HashSet<>());
    }

    // determineProviderType removed: provider now declares its own type via getType()
    
    private Set<ProviderClass> addToSet(Set<ProviderClass> existing, ProviderClass newClass) {
        Set<ProviderClass> result = new HashSet<>(existing);
        result.add(newClass);
        return result;
    }
    
    private boolean isSimilar(String requested, String provider) {
        // Exact match (shouldn't happen, but just in case)
        if (requested.equals(provider)) {
            return true;
        }
        
        // Provider starts with requested (e.g., "gpt" matches "gpt-5-nano")
        if (provider.startsWith(requested)) {
            return true;
        }
        
        // Provider contains requested (e.g., "nano" matches "gpt-5-nano")
        if (provider.contains(requested)) {
            return true;
        }
        
        // Levenshtein distance for typos (e.g., "dummi" matches "dummy")
        if (levenshteinDistance(requested, provider) <= 3) {
            return true;
        }
        
        // Check if requested is a reasonable abbreviation
        if (requested.length() >= 3 && provider.length() > requested.length()) {
            // Check if all characters of requested appear in order in provider
            if (isSubsequence(requested, provider)) {
                return true;
            }
        }
        
        return false;
    }
    
    private int calculateSimilarityScore(String requested, String provider) {
        // Lower score = better match (for sorting)
        
        // Exact match (best)
        if (requested.equals(provider)) {
            return 0;
        }
        
        // Provider starts with requested (very good)
        if (provider.startsWith(requested)) {
            return 1 + (provider.length() - requested.length()); // Prefer shorter extensions
        }
        
        // Provider contains requested (good)
        if (provider.contains(requested)) {
            return 100 + provider.indexOf(requested); // Prefer matches near the beginning
        }
        
        // Levenshtein distance for typos
        int editDistance = levenshteinDistance(requested, provider);
        if (editDistance <= 3) {
            return 1000 + editDistance; // Prefer fewer edits
        }
        
        // Subsequence match (abbreviation)
        if (isSubsequence(requested, provider)) {
            return 10000 + provider.length(); // Prefer shorter overall names
        }
        
        return Integer.MAX_VALUE; // Should not happen due to filtering
    }
    
    private boolean isSubsequence(String s, String t) {
        int i = 0; // Index for s
        int j = 0; // Index for t
        
        while (i < s.length() && j < t.length()) {
            if (s.charAt(i) == t.charAt(j)) {
                i++;
            }
            j++;
        }
        
        return i == s.length(); // All characters of s were found in order in t
    }
    
    private boolean isLevenshteinSimilar(String str1, String str2, int maxDistance) {
        return levenshteinDistance(str1, str2) <= maxDistance;
    }
    
    private int levenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        
        return dp[len1][len2];
    }
}
