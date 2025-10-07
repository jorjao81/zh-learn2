package com.zhlearn.infrastructure.dummy;

import java.util.List;
import java.util.Optional;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;

public class DummyExampleProvider implements ExampleProvider {

    @Override
    public String getName() {
        return "dummy";
    }

    @Override
    public String getDescription() {
        return "Test provider that returns dummy usage examples for development and testing";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DUMMY;
    }

    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        String contextSuffix = definition.map(def -> " (meaning: " + def + ")").orElse("");

        // Only provide breakdown for single characters
        String breakdown1 =
                word.characters().length() == 1 ? word.characters() + " + 计 (calculation)" : null;
        String breakdown2 =
                word.characters().length() == 1 ? word.characters() + " + 价 (price/value)" : null;

        List<Example.Usage> usages =
                List.of(
                        new Example.Usage(
                                word.characters() + "计",
                                word.characters() + "jì",
                                "to estimate; calculation",
                                "dummy context" + contextSuffix,
                                breakdown1),
                        new Example.Usage(
                                word.characters() + "价",
                                word.characters() + "jià",
                                "to appraise; valuation",
                                "dummy context 2" + contextSuffix,
                                breakdown2));
        return new Example(usages, List.of());
    }
}
