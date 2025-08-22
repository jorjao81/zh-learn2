module com.zhlearn.infrastructure {
    requires com.zhlearn.domain;
    requires java.base;
    requires java.logging;
    requires java.net.http;
    
    // LangChain4j automatic modules (using jar names)
    requires transitive langchain4j.core;
    requires langchain4j.open.ai;
    requires langchain4j.google.ai.gemini;

    exports com.zhlearn.infrastructure.dummy;
    
    provides com.zhlearn.domain.provider.PinyinProvider 
        with com.zhlearn.infrastructure.dummy.DummyPinyinProvider;
    
    provides com.zhlearn.domain.provider.DefinitionProvider 
        with com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
    
    provides com.zhlearn.domain.provider.StructuralDecompositionProvider 
        with com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;
    
    provides com.zhlearn.domain.provider.ExampleProvider 
        with com.zhlearn.infrastructure.dummy.DummyExampleProvider;
    
    provides com.zhlearn.domain.provider.ExplanationProvider 
        with com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
}