module com.zhlearn.application {
    requires transitive com.zhlearn.domain;
    requires transitive com.zhlearn.infrastructure;
    requires org.slf4j;

    exports com.zhlearn.application.service;
    exports com.zhlearn.application.format;
    exports com.zhlearn.application.audio;
    exports com.zhlearn.application.export;

    uses com.zhlearn.domain.provider.PinyinProvider;
    uses com.zhlearn.domain.provider.DefinitionProvider;
    uses com.zhlearn.domain.provider.StructuralDecompositionProvider;
    uses com.zhlearn.domain.provider.ExampleProvider;
    uses com.zhlearn.domain.provider.ExplanationProvider;
    uses com.zhlearn.domain.provider.AudioProvider;
}
