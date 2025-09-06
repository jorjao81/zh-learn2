module com.zhlearn.cli {
    requires com.zhlearn.domain;
    requires com.zhlearn.infrastructure;
    requires com.zhlearn.application;
    requires io.helidon.config;
    requires java.logging;
    requires java.base;
    requires org.fusesource.jansi;
    requires org.jsoup;
    requires info.picocli;
    
    uses com.zhlearn.domain.provider.PinyinProvider;
    uses com.zhlearn.domain.provider.DefinitionProvider;
    uses com.zhlearn.domain.provider.StructuralDecompositionProvider;
    uses com.zhlearn.domain.provider.ExampleProvider;
    uses com.zhlearn.domain.provider.ExplanationProvider;
    uses com.zhlearn.domain.provider.AudioProvider;
    uses com.zhlearn.domain.dictionary.Dictionary;
    
    exports com.zhlearn.cli;
    opens com.zhlearn.cli to info.picocli;
}
