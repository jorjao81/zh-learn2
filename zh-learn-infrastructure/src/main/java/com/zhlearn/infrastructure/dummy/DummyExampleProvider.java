package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.provider.ExampleProvider;

import java.util.List;
import java.util.Optional;

public class DummyExampleProvider implements ExampleProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        String contextSuffix = definition.map(def -> " (meaning: " + def + ")").orElse("");
        
        var usages = List.of(
            new Example.Usage(
                "这是一个包含 " + word.characters() + " 的句子。",
                "zhè shì yīgè bāohán " + word.characters() + " de jùzi",
                "This is a sentence containing " + word.characters() + ".",
                "dummy context" + contextSuffix
            ),
            new Example.Usage(
                "另一个 " + word.characters() + " 的例子。",
                "lìng yīgè " + word.characters() + " de lìzi",
                "Another example with " + word.characters() + ".",
                "dummy context 2" + contextSuffix
            )
        );
        return new Example(usages);
    }
}