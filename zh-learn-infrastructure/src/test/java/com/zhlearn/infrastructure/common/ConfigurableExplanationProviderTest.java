package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurableExplanationProviderTest {

    @Test
    void shouldRouteSingleCharacterRequestsToSingleProcessor() {
        Explanation single = new Explanation("single explanation");
        Explanation multi = new Explanation("multi explanation");
        AtomicReference<String> invocations = new AtomicReference<>();

        ConfigurableExplanationProvider provider = new ConfigurableExplanationProvider(
            hanzi -> {
                invocations.set("single");
                return single;
            },
            hanzi -> {
                invocations.set("multi");
                return multi;
            },
            "test",
            "Test provider"
        );

        Explanation result = provider.getExplanation(new Hanzi("学"));

        assertThat(result).isSameAs(single);
        assertThat(invocations.get()).isEqualTo("single");
    }

    @Test
    void shouldRouteMultiCharacterRequestsToMultiProcessor() {
        Explanation single = new Explanation("single explanation");
        Explanation multi = new Explanation("multi explanation");
        AtomicReference<String> invocations = new AtomicReference<>();

        ConfigurableExplanationProvider provider = new ConfigurableExplanationProvider(
            hanzi -> {
                invocations.set("single");
                return single;
            },
            hanzi -> {
                invocations.set("multi");
                return multi;
            },
            "test",
            "Test provider"
        );

        Explanation result = provider.getExplanation(new Hanzi("学校"));

        assertThat(result).isSameAs(multi);
        assertThat(invocations.get()).isEqualTo("multi");
    }

    @Test
    void shouldExposeConfigsWhenConstructedWithProviderConfigs() {
        ProviderConfig<Explanation> singleConfig = new ProviderConfig<>(
            "api",
            "base",
            "model",
            0.1,
            100,
            "/template",
            "/examples/",
            Explanation::new,
            "name",
            "error"
        );
        ProviderConfig<Explanation> multiConfig = new ProviderConfig<>(
            "api2",
            "base2",
            "model2",
            0.1,
            100,
            "/template2",
            "/examples2/",
            Explanation::new,
            "name2",
            "error2"
        );

        ConfigurableExplanationProvider provider = new ConfigurableExplanationProvider(
            hanzi -> singleConfig.getResponseMapper().apply("single"),
            singleConfig,
            hanzi -> multiConfig.getResponseMapper().apply("multi"),
            multiConfig,
            "test",
            "Test provider"
        );

        assertThat(provider.singleCharConfig()).containsSame(singleConfig);
        assertThat(provider.multiCharConfig()).containsSame(multiConfig);
    }
}
