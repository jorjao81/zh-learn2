package com.zhlearn.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProviderConfigurationTest {

    @Test
    void audioProviderDefaultsToDefaultWhenNull() {
        ProviderConfiguration cfg =
                new ProviderConfiguration(
                        "dummy-default", // default
                        null, // pinyin
                        null, // definition
                        null, // definitionFormatter
                        null, // decomposition
                        null, // example
                        null, // explanation
                        null // audio
                        );

        assertThat(cfg.getAudioProvider()).isEqualTo("dummy-default");
    }

    @Test
    void returnsExplicitAudioProviderWhenProvided() {
        ProviderConfiguration cfg =
                new ProviderConfiguration(
                        "dummy-default", null, null, null, null, null, null, "anki");

        assertThat(cfg.getAudioProvider()).isEqualTo("anki");
    }
}
