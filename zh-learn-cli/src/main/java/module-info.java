module com.zhlearn.cli {
    requires com.zhlearn.domain;
    requires com.zhlearn.infrastructure;
    requires com.zhlearn.application;
    requires io.helidon.config;
    requires java.logging;
    requires java.base;
    
    uses com.zhlearn.domain.provider.PinyinProvider;
    uses com.zhlearn.domain.provider.DefinitionProvider;
    uses com.zhlearn.domain.provider.StructuralDecompositionProvider;
    uses com.zhlearn.domain.provider.ExampleProvider;
    uses com.zhlearn.domain.provider.ExplanationProvider;
    
    exports com.zhlearn.cli;
}