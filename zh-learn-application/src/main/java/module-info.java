module com.zhlearn.application {
    requires com.zhlearn.domain;
    requires com.zhlearn.infrastructure;
    
    exports com.zhlearn.application.service;
    exports com.zhlearn.application.format;
    uses com.zhlearn.domain.provider.AudioProvider;
}
