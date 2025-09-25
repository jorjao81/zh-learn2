package com.zhlearn.infrastructure.qwen;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class QwenTtsClientTest {

    @Test
    void usesSingaporeDashScopeEndpoint() throws Exception {
        Field endpointField = QwenTtsClient.class.getDeclaredField("ENDPOINT");
        endpointField.setAccessible(true);
        URI endpoint = (URI) endpointField.get(null);

        assertThat(endpoint.getHost()).isEqualTo("dashscope-intl.aliyuncs.com");
        assertThat(endpoint.getPath()).isEqualTo("/api/v1/services/aigc/multimodal-generation/generation");
        assertThat(endpoint.getScheme()).isEqualTo("https");
    }
}

