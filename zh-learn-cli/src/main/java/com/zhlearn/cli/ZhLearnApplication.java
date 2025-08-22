package com.zhlearn.cli;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.infrastructure.dummy.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ZhLearnApplication {
    
    private final WordAnalysisServiceImpl wordAnalysisService;

    public ZhLearnApplication() {

        ProviderRegistry registry = new ProviderRegistry();
        registry.registerDefinitionProvider(new DummyDefinitionProvider());
        registry.registerExampleProvider(new DummyExampleProvider());
        registry.registerExplanationProvider(new DummyExplanationProvider());
        registry.registerPinyinProvider(new DummyPinyinProvider());
        registry.registerStructuralDecompositionProvider(new DummyStructuralDecompositionProvider());

        this.wordAnalysisService = new WordAnalysisServiceImpl(registry);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }
        
        String command = args[0];
        if (!"word".equals(command)) {
            System.err.println("Unknown command: " + command);
            printUsage();
            System.exit(1);
        }
        
        String chineseWord = args[1];
        String providerName = extractProviderName(args);
        
        if (providerName == null) {
            providerName = "dummy";
        }
        
        ZhLearnApplication app = new ZhLearnApplication();
        app.analyzeWord(chineseWord, providerName);
    }
    
    private static String extractProviderName(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--provider".equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }
    
    private void analyzeWord(String wordStr, String providerName) {
        try {
            ChineseWord word = new ChineseWord(wordStr);
            WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, providerName);
            
            printAnalysis(analysis);
        } catch (Exception e) {
            System.err.println("Error analyzing word: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void printAnalysis(WordAnalysis analysis) {
        System.out.println("Chinese Word: " + analysis.word().characters());
        System.out.println("Provider: " + analysis.providerName());
        System.out.println();
        
        System.out.println("Pinyin: " + analysis.pinyin().pinyin());
        System.out.println();
        
        System.out.println("Definition: " + analysis.definition().meaning());
        System.out.println("Part of Speech: " + analysis.definition().partOfSpeech());
        System.out.println();
        
        System.out.println("Structural Decomposition: " + analysis.structuralDecomposition().decomposition());
        System.out.println();
        
        System.out.println("Examples:");
        for (var usage : analysis.examples().usages()) {
            System.out.println("  Chinese: " + usage.sentence());
            System.out.println("  Pinyin: " + usage.pinyin());
            System.out.println("  English: " + usage.translation());
            if (usage.context() != null && !usage.context().isEmpty()) {
                System.out.println("  Context: " + usage.context());
            }
            System.out.println();
        }
        
        System.out.println("Explanation: " + analysis.explanation().explanation());
    }
    
    private static void printUsage() {
        System.out.println("Usage: zh-learn word <chinese_word> [--provider <provider_name>]");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  zh-learn word 汉语");
        System.out.println("  zh-learn word 汉语 --provider gpt-4o");
        System.out.println("  zh-learn word 学习 --provider gemini-2.5-pro");
        System.out.println();
        System.out.println("Available providers:");
        System.out.println("  Dummy: dummy-pinyin, dummy-definition, dummy-structural-decomposition, dummy-example, dummy-explanation");
        System.out.println("  GPT: gpt-4o, gpt-4o-mini, gpt-5, gpt-5-nano");
        System.out.println("  Gemini: gemini-2.0-flash-exp, gemini-2.5-pro, gemini-2.5-flash");
        System.out.println("  Qwen: qwen-turbo, qwen-plus, qwen-max");
        System.out.println("  DeepSeek: deepseek-chat, deepseek-coder");
        System.out.println("  Grok: grok-4, grok-4-turbo");
    }
}