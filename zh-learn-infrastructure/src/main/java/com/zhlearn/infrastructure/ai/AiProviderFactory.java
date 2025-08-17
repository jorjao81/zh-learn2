package com.zhlearn.infrastructure.ai;

import com.zhlearn.domain.provider.*;
import dev.langchain4j.model.chat.ChatModel;

import java.util.Map;

public class AiProviderFactory {
    
    public static PinyinProvider createPinyinProvider(String providerName, Map<String, String> config) {
        AiProviderConfig aiConfig = createPinyinConfig(providerName);
        ChatModel model = createModel(providerName, config);
        return new AiPinyinProvider(aiConfig, model);
    }
    
    public static DefinitionProvider createDefinitionProvider(String providerName, Map<String, String> config) {
        AiProviderConfig aiConfig = createDefinitionConfig(providerName);
        ChatModel model = createModel(providerName, config);
        return new AiDefinitionProvider(aiConfig, model);
    }
    
    public static StructuralDecompositionProvider createStructuralDecompositionProvider(String providerName, Map<String, String> config) {
        AiProviderConfig aiConfig = createStructuralDecompositionConfig(providerName);
        ChatModel model = createModel(providerName, config);
        return new AiStructuralDecompositionProvider(aiConfig, model);
    }
    
    public static ExampleProvider createExampleProvider(String providerName, Map<String, String> config) {
        AiProviderConfig aiConfig = createExampleConfig(providerName);
        ChatModel model = createModel(providerName, config);
        return new AiExampleProvider(aiConfig, model);
    }
    
    public static ExplanationProvider createExplanationProvider(String providerName, Map<String, String> config) {
        AiProviderConfig aiConfig = createExplanationConfig(providerName);
        ChatModel model = createModel(providerName, config);
        return new AiExplanationProvider(aiConfig, model);
    }
    
    private static ChatModel createModel(String providerName, Map<String, String> config) {
        return switch (providerName.toLowerCase()) {
            case "gpt-4o", "gpt-4o-mini", "gpt-5", "gpt-5-nano" -> 
                ModelFactory.createOpenAiModel(extractModelName(providerName), config);
            case "gemini-2.0-flash-exp", "gemini-2.5-pro", "gemini-2.5-flash" -> 
                ModelFactory.createGeminiModel(extractModelName(providerName), config);
            case "qwen-turbo", "qwen-plus", "qwen-max" -> 
                ModelFactory.createGenericOpenAiCompatibleModel(extractModelName(providerName), config);
            case "deepseek-chat", "deepseek-coder" -> 
                ModelFactory.createGenericOpenAiCompatibleModel(extractModelName(providerName), config);
            case "grok-4", "grok-4-turbo" -> 
                ModelFactory.createGenericOpenAiCompatibleModel(extractModelName(providerName), config);
            default -> throw new IllegalArgumentException("Unsupported AI provider: " + providerName);
        };
    }
    
    private static String extractModelName(String providerName) {
        return providerName;
    }
    
    private static AiProviderConfig createPinyinConfig(String providerName) {
        return new AiProviderConfig(
            providerName + "-pinyin",
            """
            You are an expert in Chinese linguistics. Given a Chinese word, provide its Pinyin romanization with tone marks.
            
            Format your response exactly as:
            romanization: [pinyin without tones]
            tone_marks: [pinyin with tone marks]
            """,
            java.util.List.of(
                "Input: 汉语\nromanization: hanyu\ntone_marks: hànyǔ",
                "Input: 学习\nromanization: xuexi\ntone_marks: xuéxí"
            ),
            Map.of()
        );
    }
    
    private static AiProviderConfig createDefinitionConfig(String providerName) {
        return new AiProviderConfig(
            providerName + "-definition",
            """
            You are an expert in Chinese language. Given a Chinese word, provide its definition and part of speech.
            
            Format your response exactly as:
            meaning: [English definition]
            part_of_speech: [noun/verb/adjective/etc.]
            """,
            java.util.List.of(
                "Input: 汉语\nmeaning: Chinese language (spoken)\npart_of_speech: noun",
                "Input: 学习\nmeaning: to study, to learn\npart_of_speech: verb"
            ),
            Map.of()
        );
    }
    
    private static AiProviderConfig createStructuralDecompositionConfig(String providerName) {
        return new AiProviderConfig(
            providerName + "-decomposition",
            """
            You are an expert in Chinese character structure. Given a Chinese word, provide its structural decomposition.
            
            Format each component as:
            component: [character] | [meaning] | [radical]
            """,
            java.util.List.of(
                "Input: 汉语\ncomponent: 汉 | Han Chinese | 氵\ncomponent: 语 | language/speech | 言",
                "Input: 学习\ncomponent: 学 | study/learn | 子\ncomponent: 习 | practice | 羽"
            ),
            Map.of()
        );
    }
    
    private static AiProviderConfig createExampleConfig(String providerName) {
        return new AiProviderConfig(
            providerName + "-examples",
            """
            You are an expert in Chinese language usage. Given a Chinese word, provide example sentences.
            
            Format each example as:
            example: [Chinese sentence] | [English translation] | [context]
            """,
            java.util.List.of(
                "Input: 汉语\nexample: 我正在学习汉语。 | I am learning Chinese. | education\nexample: 汉语是世界上使用人数最多的语言。 | Chinese is the most spoken language in the world. | general",
                "Input: 学习\nexample: 她每天都在学习新知识。 | She learns new knowledge every day. | education\nexample: 学习需要坚持不懈。 | Learning requires persistence. | motivation"
            ),
            Map.of()
        );
    }
    
    private static AiProviderConfig createExplanationConfig(String providerName) {
        return new AiProviderConfig(
            providerName + "-explanation",
            """
            You are an expert in Chinese etymology and cultural linguistics. Given a Chinese word, provide comprehensive explanation.
            
            Format your response exactly as:
            etymology: [word origins and historical development]
            usage: [how the word is used in modern Chinese]
            similar_words: [comma-separated list of similar words]
            cultural_context: [cultural significance and context]
            """,
            java.util.List.of(
                "Input: 汉语\netymology: The term combines 汉 (Han, referring to the Han dynasty and ethnic group) with 语 (language), literally meaning 'language of the Han people'\nusage: Commonly used to refer to the Chinese language, especially spoken Chinese, as opposed to 中文 which often refers to written Chinese\nsimilar_words: 中文,华语,普通话,国语\ncultural_context: Reflects the ethnic and cultural identity of the Han Chinese people, the majority ethnic group in China"
            ),
            Map.of()
        );
    }
}