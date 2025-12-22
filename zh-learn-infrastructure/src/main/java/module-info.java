module com.zhlearn.infrastructure {
    requires com.zhlearn.domain;
    requires java.logging;
    requires java.net.http;
    requires java.sql;

    // LangChain4j automatic modules (using jar names)
    requires transitive langchain4j.core;
    requires langchain4j.open.ai;
    requires langchain4j.google.ai.gemini;

    // SLF4J logging modules
    requires org.slf4j;

    // Helidon Fault Tolerance for retry with exponential backoff on rate limits
    requires io.helidon.faulttolerance;

    // Jackson JSON processing modules
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    // Apache Commons CSV for Anki parsing
    requires org.apache.commons.csv;

    // Pinyin4j for Chinese to Pinyin conversion
    requires pinyin4j;

    // Tencent Cloud SDK for TTS
    requires tencentcloud.sdk.java.common;
    requires tencentcloud.sdk.java.tts;
    requires com.google.gson;
    requires kotlin.stdlib;

    // Internal pinyin utilities (tone converter)
    requires com.zhlearn.pinyin;

    exports com.zhlearn.infrastructure.dummy;
    exports com.zhlearn.infrastructure.common;
    exports com.zhlearn.infrastructure.anki;
    exports com.zhlearn.infrastructure.dictionary;
    exports com.zhlearn.infrastructure.audio;
    exports com.zhlearn.infrastructure.cache;
    exports com.zhlearn.infrastructure.pinyin4j;
    exports com.zhlearn.infrastructure.passthrough;
    // pinyin utility moved to separate module com.zhlearn.pinyin
    exports com.zhlearn.infrastructure.pleco;
    exports com.zhlearn.infrastructure.qwen;
    exports com.zhlearn.infrastructure.forvo;
    exports com.zhlearn.infrastructure.tencent;
    exports com.zhlearn.infrastructure.ratelimit;
    exports com.zhlearn.infrastructure.minimax;
}
