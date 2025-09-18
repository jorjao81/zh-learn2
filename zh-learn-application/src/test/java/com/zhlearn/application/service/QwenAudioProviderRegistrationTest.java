package com.zhlearn.application.service;

import com.zhlearn.domain.model.ProviderInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QwenAudioProviderRegistrationTest {

    @Test
    void qwenProviderIsDiscoveredViaServiceLoader() {
        ProviderRegistry registry = new ProviderRegistry();

        assertThat(registry.getAvailableAudioProviders()).contains("qwen-tts");
        assertThat(registry.getAllProviderInfo())
            .anySatisfy(info -> {
                if (info.name().equals("qwen-tts")) {
                    assertThat(info.type()).isEqualTo(ProviderInfo.ProviderType.AI);
                    assertThat(info.supportedClasses()).contains(ProviderInfo.ProviderClass.AUDIO);
                }
            });
    }
}
