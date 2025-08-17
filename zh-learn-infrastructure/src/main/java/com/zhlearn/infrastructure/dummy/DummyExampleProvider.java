package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.provider.ExampleProvider;

import java.util.List;

public class DummyExampleProvider implements ExampleProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public Example getExamples(ChineseWord word) {
        var usages = List.of(
            new Example.Usage(
                "这是一个包含 " + word.characters() + " 的句子。",
                "zhè shì yīgè bāohán " + word.characters() + " de jùzi",
                "This is a sentence containing " + word.characters() + ".",
                "dummy context"
            ),
            new Example.Usage(
                "另一个 " + word.characters() + " 的例子。",
                "lìng yīgè " + word.characters() + " de lìzi",
                "Another example with " + word.characters() + ".",
                "dummy context 2"
            )
        );
        return new Example(usages);
    }
}