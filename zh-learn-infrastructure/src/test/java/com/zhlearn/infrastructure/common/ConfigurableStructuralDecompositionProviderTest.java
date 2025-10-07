package com.zhlearn.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;

class ConfigurableStructuralDecompositionProviderTest {

    @Test
    void shouldRouteSingleCharacterRequestsToSingleProcessor() {
        StructuralDecomposition single = new StructuralDecomposition("single");
        StructuralDecomposition multi = new StructuralDecomposition("multi");
        AtomicReference<String> invocations = new AtomicReference<>();

        ConfigurableStructuralDecompositionProvider provider =
                new ConfigurableStructuralDecompositionProvider(
                        hanzi -> {
                            invocations.set("single");
                            return single;
                        },
                        hanzi -> {
                            invocations.set("multi");
                            return multi;
                        },
                        "test",
                        "Test provider");

        StructuralDecomposition result = provider.getStructuralDecomposition(new Hanzi("学"));

        assertThat(result).isSameAs(single);
        assertThat(invocations.get()).isEqualTo("single");
    }

    @Test
    void shouldRouteMultiCharacterRequestsToMultiProcessor() {
        StructuralDecomposition single = new StructuralDecomposition("single");
        StructuralDecomposition multi = new StructuralDecomposition("multi");
        AtomicReference<String> invocations = new AtomicReference<>();

        ConfigurableStructuralDecompositionProvider provider =
                new ConfigurableStructuralDecompositionProvider(
                        hanzi -> {
                            invocations.set("single");
                            return single;
                        },
                        hanzi -> {
                            invocations.set("multi");
                            return multi;
                        },
                        "test",
                        "Test provider");

        StructuralDecomposition result = provider.getStructuralDecomposition(new Hanzi("学校"));

        assertThat(result).isSameAs(multi);
        assertThat(invocations.get()).isEqualTo("multi");
    }

    @Test
    void shouldExposeConfigsWhenConstructedWithProviderConfigs() {
        ProviderConfig<StructuralDecomposition> singleConfig =
                new ProviderConfig<>(
                        "api",
                        "base",
                        "model",
                        0.1,
                        100,
                        "/template",
                        "/examples/",
                        StructuralDecomposition::new,
                        "name",
                        "error");
        ProviderConfig<StructuralDecomposition> multiConfig =
                new ProviderConfig<>(
                        "api2",
                        "base2",
                        "model2",
                        0.1,
                        100,
                        "/template2",
                        "/examples2/",
                        StructuralDecomposition::new,
                        "name2",
                        "error2");

        ConfigurableStructuralDecompositionProvider provider =
                new ConfigurableStructuralDecompositionProvider(
                        hanzi -> singleConfig.getResponseMapper().apply("single"),
                        singleConfig,
                        hanzi -> multiConfig.getResponseMapper().apply("multi"),
                        multiConfig,
                        "test",
                        "Test provider");

        assertThat(provider.singleCharConfig()).containsSame(singleConfig);
        assertThat(provider.multiCharConfig()).containsSame(multiConfig);
    }
}
