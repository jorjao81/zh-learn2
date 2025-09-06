module com.zhlearn.infrastructure {
    requires com.zhlearn.domain;
    requires java.base;
    requires java.logging;
    requires java.net.http;
    
    // LangChain4j automatic modules (using jar names)
    requires transitive langchain4j.core;
    requires langchain4j.open.ai;
    requires langchain4j.google.ai.gemini;
    
    // SLF4J logging modules
    requires org.slf4j;

    // Jackson JSON processing modules
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    
    // Apache Commons CSV for Anki parsing
    requires org.apache.commons.csv;
    
    // Pinyin4j for Chinese to Pinyin conversion
    requires pinyin4j;

    exports com.zhlearn.infrastructure.dummy;
    exports com.zhlearn.infrastructure.deepseek;
    exports com.zhlearn.infrastructure.gpt5nano;
    exports com.zhlearn.infrastructure.common;
    exports com.zhlearn.infrastructure.anki;
    exports com.zhlearn.infrastructure.dictionary;
    exports com.zhlearn.infrastructure.cache;
    exports com.zhlearn.infrastructure.pinyin4j;
    
    provides com.zhlearn.domain.provider.PinyinProvider
        with com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;
    
    provides com.zhlearn.domain.provider.DefinitionProvider 
        with com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
    
    provides com.zhlearn.domain.provider.StructuralDecompositionProvider 
        with com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider,
             com.zhlearn.infrastructure.deepseek.DeepSeekStructuralDecompositionProvider,
             com.zhlearn.infrastructure.gpt5nano.GPT5NanoStructuralDecompositionProvider;
    
    provides com.zhlearn.domain.provider.ExampleProvider 
        with com.zhlearn.infrastructure.dummy.DummyExampleProvider,
             com.zhlearn.infrastructure.deepseek.DeepSeekExampleProvider,
             com.zhlearn.infrastructure.gpt5nano.GPT5NanoExampleProvider;
    
    provides com.zhlearn.domain.provider.ExplanationProvider 
        with com.zhlearn.infrastructure.dummy.DummyExplanationProvider,
             com.zhlearn.infrastructure.deepseek.DeepSeekExplanationProvider,
             com.zhlearn.infrastructure.gpt5nano.GPT5NanoExplanationProvider;

    provides com.zhlearn.domain.provider.AudioProvider
        with com.zhlearn.infrastructure.anki.ExistingAnkiPronunciationProvider;
}
