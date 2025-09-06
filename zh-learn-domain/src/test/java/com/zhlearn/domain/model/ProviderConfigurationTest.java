package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderConfigurationTest {

    @Test
    void audioProviderDefaultsToDefaultWhenNull() {
        ProviderConfiguration cfg = new ProviderConfiguration(
            "dummy-default", // default
            null, // pinyin
            null, // definition
            null, // decomposition
            null, // example
            null, // explanation
            null  // audio
        );

        assertThat(cfg.getAudioProvider()).isEqualTo("dummy-default");
    }

    @Test
    void returnsExplicitAudioProviderWhenProvided() {
        ProviderConfiguration cfg = new ProviderConfiguration(
            "dummy-default",
            null,
            null,
            null,
            null,
            null,
            "existing-anki-pronunciation"
        );

        assertThat(cfg.getAudioProvider()).isEqualTo("existing-anki-pronunciation");
    }
}

