package com.zhlearn.cli;

import com.zhlearn.application.audio.AudioOrchestrator;
import com.zhlearn.application.service.ParallelWordAnalysisService;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.*;
import com.zhlearn.domain.model.ProviderInfo;
import com.zhlearn.domain.model.ProviderInfo.ProviderClass;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.*;
import com.zhlearn.infrastructure.anki.AnkiPronunciationProvider;
import com.zhlearn.infrastructure.common.ExampleResponseMapper;
import com.zhlearn.infrastructure.common.GenericChatModelProvider;
import com.zhlearn.infrastructure.common.ProviderConfig;
import com.zhlearn.infrastructure.common.ZhipuChatModelProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
import com.zhlearn.infrastructure.dummy.DummyExampleProvider;
import com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
import com.zhlearn.infrastructure.dummy.DummyPinyinProvider;
import com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;
import com.zhlearn.infrastructure.forvo.ForvoAudioProvider;
import com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;
import com.zhlearn.infrastructure.qwen.QwenAudioProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.ScopeType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Command(name = "zh-learn",
        mixinStandardHelpOptions = true,
        version = "1.0.0-SNAPSHOT",
        subcommands = { WordCommand.class, ProvidersCommand.class, ParseAnkiCommand.class, ParsePlecoCommand.class, AudioCommand.class, AudioSelectCommand.class, picocli.CommandLine.HelpCommand.class },
        scope = ScopeType.INHERIT)
public class MainCommand implements Runnable {

    private final Map<String, PinyinProvider> pinyinProviders = new LinkedHashMap<>();
    private final Map<String, DefinitionProvider> definitionProviders = new LinkedHashMap<>();
    private final Map<String, StructuralDecompositionProvider> decompositionProviders = new LinkedHashMap<>();
    private final Map<String, ExampleProvider> exampleProviders = new LinkedHashMap<>();
    private final Map<String, ExplanationProvider> explanationProviders = new LinkedHashMap<>();
    private final Map<String, AudioProvider> audioProviders = new LinkedHashMap<>();

    private final WordAnalysisServiceImpl wordAnalysisService;

    public MainCommand() {
        initializeProviders();
        this.wordAnalysisService = new WordAnalysisServiceImpl(
            pinyinProviders,
            definitionProviders,
            decompositionProviders,
            exampleProviders,
            explanationProviders,
            audioProviders
        );
    }

    public WordAnalysisServiceImpl getWordAnalysisService() {
        return wordAnalysisService;
    }

    public ParallelWordAnalysisService createParallelWordAnalysisService(int threadPoolSize) {
        return new ParallelWordAnalysisService(
            wordAnalysisService,
            decompositionProviders,
            exampleProviders,
            explanationProviders,
            audioProviders,
            threadPoolSize
        );
    }

    public AudioOrchestrator createAudioOrchestrator() {
        return new AudioOrchestrator(audioProviders);
    }

    public Optional<AudioProvider> getAudioProvider(String name) {
        return Optional.ofNullable(audioProviders.get(name));
    }

    public Set<String> getAvailableAudioProviders() {
        return audioProviders.keySet();
    }

    public void addDefinitionProvider(DefinitionProvider provider) {
        definitionProviders.put(provider.getName(), provider);
    }

    public void addPinyinProvider(PinyinProvider provider) {
        pinyinProviders.put(provider.getName(), provider);
    }

    public void addAudioProvider(AudioProvider provider) {
        audioProviders.put(provider.getName(), provider);
    }

    public List<ProviderInfo> getAllProviderInfo() {
        Map<String, ProviderInfo> info = new LinkedHashMap<>();
        pinyinProviders.forEach((name, provider) -> addInfo(info, name, provider.getDescription(), provider.getType(), ProviderClass.PINYIN));
        definitionProviders.forEach((name, provider) -> addInfo(info, name, provider.getDescription(), provider.getType(), ProviderClass.DEFINITION));
        decompositionProviders.forEach((name, provider) -> addInfo(info, name, provider.getDescription(), provider.getType(), ProviderClass.STRUCTURAL_DECOMPOSITION));
        exampleProviders.forEach((name, provider) -> addInfo(info, name, provider.getDescription(), provider.getType(), ProviderClass.EXAMPLE));
        explanationProviders.forEach((name, provider) -> addInfo(info, name, provider.getDescription(), provider.getType(), ProviderClass.EXPLANATION));
        audioProviders.forEach((name, provider) -> addInfo(info, name, provider.getDescription(), provider.getType(), ProviderClass.AUDIO));
        return new ArrayList<>(info.values());
    }

    public boolean providerExists(String providerName) {
        return exampleProviders.containsKey(providerName)
            || explanationProviders.containsKey(providerName)
            || decompositionProviders.containsKey(providerName)
            || definitionProviders.containsKey(providerName)
            || pinyinProviders.containsKey(providerName)
            || audioProviders.containsKey(providerName);
    }

    public List<String> findSimilarProviders(String requestedProvider) {
        if (requestedProvider == null || requestedProvider.isBlank()) {
            return List.of();
        }
        Set<String> all = new java.util.HashSet<>();
        all.addAll(exampleProviders.keySet());
        all.addAll(explanationProviders.keySet());
        all.addAll(decompositionProviders.keySet());
        all.addAll(definitionProviders.keySet());
        all.addAll(pinyinProviders.keySet());
        all.addAll(audioProviders.keySet());

        String lower = requestedProvider.toLowerCase();
        return all.stream()
            .filter(name -> isSimilar(lower, name.toLowerCase()))
            .sorted((a, b) -> Integer.compare(
                similarityScore(lower, a.toLowerCase()),
                similarityScore(lower, b.toLowerCase())
            ))
            .limit(5)
            .toList();
    }

    @Override
    public void run() {
        // Root command is a container for subcommands
    }

    Map<String, PinyinProvider> getPinyinProviders() {
        return pinyinProviders;
    }

    Map<String, ExampleProvider> getExampleProviders() {
        return exampleProviders;
    }

    Map<String, ExplanationProvider> getExplanationProviders() {
        return explanationProviders;
    }

    Map<String, StructuralDecompositionProvider> getDecompositionProviders() {
        return decompositionProviders;
    }

    Map<String, DefinitionProvider> getDefinitionProviders() {
        return definitionProviders;
    }

    private void initializeProviders() {
        registerDummyProviders();
        registerDeepSeekProviders();
        registerGpt5NanoProviders();
        registerQwenProviders();
        registerGlmProviders();
        registerAudioProviders();
    }

    private void registerDummyProviders() {
        PinyinProvider pinyin4j = new Pinyin4jProvider();
        pinyinProviders.put(pinyin4j.getName(), pinyin4j);
        PinyinProvider dummyPinyin = new DummyPinyinProvider();
        pinyinProviders.put(dummyPinyin.getName(), dummyPinyin);

        DefinitionProvider dummyDefinition = new DummyDefinitionProvider();
        definitionProviders.put(dummyDefinition.getName(), dummyDefinition);

        StructuralDecompositionProvider dummyDecomposition = new DummyStructuralDecompositionProvider();
        decompositionProviders.put(dummyDecomposition.getName(), dummyDecomposition);

        ExampleProvider dummyExample = new DummyExampleProvider();
        exampleProviders.put(dummyExample.getName(), dummyExample);

        ExplanationProvider dummyExplanation = new DummyExplanationProvider();
        explanationProviders.put(dummyExplanation.getName(), dummyExplanation);
    }

    private void registerDeepSeekProviders() {
        String name = "deepseek-chat";
        String baseUrl = readConfig("DEEPSEEK_BASE_URL", "https://api.deepseek.com/v1");
        String apiKey = readConfig("DEEPSEEK_API_KEY");
        double temperature = 0.3;
        int maxTokens = 8000;

        exampleProviders.put(name, exampleProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                name,
                temperature,
                maxTokens,
                "/examples/prompt-template.md",
                "/examples/examples/",
                new ExampleResponseMapper(),
                name,
                "Failed to get examples from DeepSeek"
            ),
            "DeepSeek AI example provider"
        ));

        explanationProviders.put(name, explanationProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                name,
                temperature,
                maxTokens,
                "/explanation/prompt-template.md",
                "/explanation/examples/",
                Explanation::new,
                name,
                "Failed to get explanation from DeepSeek"
            ),
            "DeepSeek AI explanation provider"
        ));

        decompositionProviders.put(name, decompositionProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                name,
                temperature,
                maxTokens,
                "/structural-decomposition/prompt-template.md",
                "/structural-decomposition/examples/",
                StructuralDecomposition::new,
                name,
                "Failed to get structural decomposition from DeepSeek"
            ),
            "DeepSeek AI decomposition provider"
        ));
    }

    private void registerGpt5NanoProviders() {
        String name = "gpt-5-nano";
        String baseUrl = readConfig("OPENAI_BASE_URL", "https://api.openai.com/v1");
        String apiKey = readConfig("OPENAI_API_KEY");

        exampleProviders.put(name, exampleProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                name,
                null,
                null,
                "/examples/prompt-template.md",
                "/examples/examples/",
                new ExampleResponseMapper(),
                name,
                "Failed to get examples from GPT-5 Nano"
            ),
            "OpenAI GPT-5 Nano example provider"
        ));

        explanationProviders.put(name, explanationProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                name,
                null,
                null,
                "/explanation/prompt-template.md",
                "/explanation/examples/",
                Explanation::new,
                name,
                "Failed to get explanation from GPT-5 Nano"
            ),
            "OpenAI GPT-5 Nano explanation provider"
        ));

        decompositionProviders.put(name, decompositionProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                name,
                null,
                null,
                "/structural-decomposition/prompt-template.md",
                "/structural-decomposition/examples/",
                StructuralDecomposition::new,
                name,
                "Failed to get structural decomposition from GPT-5 Nano"
            ),
            "OpenAI GPT-5 Nano decomposition provider"
        ));
    }

    private void registerQwenProviders() {
        registerQwenVariant("qwen3-max", "qwen3-max-preview", "Qwen3 Max AI provider");
        registerQwenVariant("qwen3-plus", "qwen-plus-latest", "Qwen3 Plus AI provider");
        registerQwenVariant("qwen3-flash", "qwen-turbo-latest", "Qwen3 Flash AI provider");
    }

    private void registerQwenVariant(String name, String modelName, String description) {
        String baseUrl = readConfig("DASHSCOPE_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1");
        String apiKey = readConfig("DASHSCOPE_API_KEY");
        double temperature = 0.3;
        int maxTokens = 8000;

        exampleProviders.put(name, exampleProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                modelName,
                temperature,
                maxTokens,
                "/examples/prompt-template.md",
                "/examples/examples/",
                new ExampleResponseMapper(),
                name,
                "Failed to get examples from " + name
            ),
            description + " (examples)"
        ));

        explanationProviders.put(name, explanationProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                modelName,
                temperature,
                maxTokens,
                "/explanation/prompt-template.md",
                "/explanation/examples/",
                Explanation::new,
                name,
                "Failed to get explanation from " + name
            ),
            description + " (explanations)"
        ));

        decompositionProviders.put(name, decompositionProvider(
            new ProviderConfig<>(
                apiKey,
                baseUrl,
                modelName,
                temperature,
                maxTokens,
                "/structural-decomposition/prompt-template.md",
                "/structural-decomposition/examples/",
                StructuralDecomposition::new,
                name,
                "Failed to get structural decomposition from " + name
            ),
            description + " (decomposition)"
        ));
    }

    private void registerGlmProviders() {
        registerGlmVariant("glm-4-flash", "glm-4-flash", "ChatGLM (z.ai) provider");
        registerGlmVariant("glm-4.5", "glm-4.5", "ChatGLM (z.ai) provider (glm-4.5)");
    }

    private void registerGlmVariant(String name, String modelName, String description) {
        String baseUrl = readConfig("ZAI_BASE_URL", readConfig("CHAT_GLM_BASE_URL", "https://api.z.ai/openai/v1"));
        String apiKey = Optional.ofNullable(readConfig("ZAI_API_KEY")).orElse(readConfig("CHAT_GLM_API_KEY"));
        double temperature = 0.3;
        int maxTokens = 8000;

        exampleProviders.put(name, glmExampleProvider(name, description, apiKey, baseUrl, modelName, temperature, maxTokens));
        explanationProviders.put(name, glmExplanationProvider(name, description, apiKey, baseUrl, modelName, temperature, maxTokens));
        decompositionProviders.put(name, glmDecompositionProvider(name, description, apiKey, baseUrl, modelName, temperature, maxTokens));
    }

    private ExampleProvider glmExampleProvider(String name, String description, String apiKey, String baseUrl, String modelName, double temperature, int maxTokens) {
        ProviderConfig<Example> config = new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            temperature,
            maxTokens,
            "/examples/prompt-template.md",
            "/examples/examples/",
            new ExampleResponseMapper(),
            name,
            "Failed to get examples from " + name
        );
        ZhipuChatModelProvider<Example> delegate = new ZhipuChatModelProvider<>(config);
        return new ExampleProvider() {
            @Override
            public String getName() { return name; }

            @Override
            public String getDescription() { return description + " (examples)"; }

            @Override
            public ProviderType getType() { return ProviderType.AI; }

            @Override
            public Example getExamples(Hanzi word, Optional<String> definition) {
                return delegate.process(word, definition);
            }
        };
    }

    private ExplanationProvider glmExplanationProvider(String name, String description, String apiKey, String baseUrl, String modelName, double temperature, int maxTokens) {
        ProviderConfig<Explanation> config = new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            temperature,
            maxTokens,
            "/explanation/prompt-template.md",
            "/explanation/examples/",
            Explanation::new,
            name,
            "Failed to get explanation from " + name
        );
        ZhipuChatModelProvider<Explanation> delegate = new ZhipuChatModelProvider<>(config);
        return new ExplanationProvider() {
            @Override
            public String getName() { return name; }

            @Override
            public String getDescription() { return description + " (explanations)"; }

            @Override
            public ProviderType getType() { return ProviderType.AI; }

            @Override
            public Explanation getExplanation(Hanzi word) {
                return delegate.process(word);
            }
        };
    }

    private StructuralDecompositionProvider glmDecompositionProvider(String name, String description, String apiKey, String baseUrl, String modelName, double temperature, int maxTokens) {
        ProviderConfig<StructuralDecomposition> config = new ProviderConfig<>(
            apiKey,
            baseUrl,
            modelName,
            temperature,
            maxTokens,
            "/structural-decomposition/prompt-template.md",
            "/structural-decomposition/examples/",
            StructuralDecomposition::new,
            name,
            "Failed to get structural decomposition from " + name
        );
        ZhipuChatModelProvider<StructuralDecomposition> delegate = new ZhipuChatModelProvider<>(config);
        return new StructuralDecompositionProvider() {
            @Override
            public String getName() { return name; }

            @Override
            public String getDescription() { return description + " (decomposition)"; }

            @Override
            public ProviderType getType() { return ProviderType.AI; }

            @Override
            public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
                return delegate.process(word);
            }
        };
    }

    private void registerAudioProviders() {
        AudioProvider anki = new AnkiPronunciationProvider();
        audioProviders.put(anki.getName(), anki);

        AudioProvider forvo = new ForvoAudioProvider();
        audioProviders.put(forvo.getName(), forvo);

        AudioProvider qwen = new QwenAudioProvider();
        audioProviders.put(qwen.getName(), qwen);
    }

    private ExampleProvider exampleProvider(ProviderConfig<Example> config, String description) {
        GenericChatModelProvider<Example> provider = new GenericChatModelProvider<>(config);
        return new ExampleProvider() {
            @Override
            public String getName() { return config.providerName(); }

            @Override
            public String getDescription() { return description; }

            @Override
            public ProviderType getType() { return ProviderType.AI; }

            @Override
            public Example getExamples(Hanzi word, Optional<String> definition) {
                return provider.process(word, definition);
            }
        };
    }

    private ExplanationProvider explanationProvider(ProviderConfig<Explanation> config, String description) {
        GenericChatModelProvider<Explanation> provider = new GenericChatModelProvider<>(config);
        return new ExplanationProvider() {
            @Override
            public String getName() { return config.providerName(); }

            @Override
            public String getDescription() { return description; }

            @Override
            public ProviderType getType() { return ProviderType.AI; }

            @Override
            public Explanation getExplanation(Hanzi word) {
                return provider.process(word);
            }
        };
    }

    private StructuralDecompositionProvider decompositionProvider(ProviderConfig<StructuralDecomposition> config, String description) {
        GenericChatModelProvider<StructuralDecomposition> provider = new GenericChatModelProvider<>(config);
        return new StructuralDecompositionProvider() {
            @Override
            public String getName() { return config.providerName(); }

            @Override
            public String getDescription() { return description; }

            @Override
            public ProviderType getType() { return ProviderType.AI; }

            @Override
            public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
                return provider.process(word);
            }
        };
    }

    private void addInfo(Map<String, ProviderInfo> info,
                         String name,
                         String description,
                         ProviderType type,
                         ProviderClass providerClass) {
        info.merge(
            name,
            new ProviderInfo(name, description, type, Set.of(providerClass)),
            (existing, incoming) -> {
                EnumSet<ProviderClass> merged = EnumSet.copyOf(existing.supportedClasses());
                merged.add(providerClass);
                return new ProviderInfo(existing.name(), existing.description(), existing.type(), merged);
            }
        );
    }

    private static boolean isSimilar(String requested, String candidate) {
        if (requested.equals(candidate)) return true;
        if (candidate.startsWith(requested)) return true;
        if (candidate.contains(requested)) return true;
        if (levenshteinDistance(requested, candidate) <= 3) return true;
        return requested.length() >= 3 && candidate.length() > requested.length() && isSubsequence(requested, candidate);
    }

    private static int similarityScore(String requested, String candidate) {
        if (requested.equals(candidate)) return 0;
        if (candidate.startsWith(requested)) return 1 + (candidate.length() - requested.length());
        if (candidate.contains(requested)) return 100 + candidate.indexOf(requested);
        int distance = levenshteinDistance(requested, candidate);
        if (distance <= 3) return 1000 + distance;
        if (isSubsequence(requested, candidate)) return 10000 + candidate.length();
        return Integer.MAX_VALUE;
    }

    private static boolean isSubsequence(String s, String t) {
        int i = 0;
        int j = 0;
        while (i < s.length() && j < t.length()) {
            if (s.charAt(i) == t.charAt(j)) {
                i++;
            }
            j++;
        }
        return i == s.length();
    }

    private static int levenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[len1][len2];
    }

    private static String readConfig(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value;
    }

    private static String readConfig(String key, String defaultValue) {
        String value = readConfig(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
