package com.zhlearn.cli;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.infrastructure.dummy.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

public class WordAnalysisStepDefinitions {

    private WordAnalysis analysis;
    private Exception lastException;

    @Given("the ZH Learn application is available")
    public void the_zh_learn_application_is_available() {
        // Application is always available in tests
        // Just verify we can create basic components
        assertNotNull(new Hanzi("你好"));
    }

    @When("I analyze the word {string} using provider {string}")
    public void i_analyze_the_word_using_provider(String word, String providerName) {
        try {
            // Create a simple analysis using dummy providers for testing
            Hanzi hanzi = new Hanzi(word);

            // Create dummy providers for testing
            var pinyinProvider = new DummyPinyinProvider();
            var definitionProvider = new DummyDefinitionProvider();
            var exampleProvider = new DummyExampleProvider();
            var explanationProvider = new DummyExplanationProvider();
            var decompositionProvider = new DummyStructuralDecompositionProvider();
            var audioProvider = new DummyAudioProvider();

            // Create a basic analysis
            this.analysis = new WordAnalysis(
                hanzi,
                pinyinProvider.getPinyin(hanzi),
                definitionProvider.getDefinition(hanzi),
                decompositionProvider.getStructuralDecomposition(hanzi),
                exampleProvider.getExamples(hanzi, java.util.Optional.empty()),
                explanationProvider.getExplanation(hanzi),
                audioProvider.getPronunciation(hanzi, pinyinProvider.getPinyin(hanzi))
            );

            this.lastException = null;
        } catch (Exception e) {
            this.lastException = e;
            this.analysis = null;
        }
    }

    @Then("the analysis should be successful")
    public void the_analysis_should_be_successful() {
        if (lastException != null) {
            fail("Analysis failed with exception: " + lastException.getMessage(), lastException);
        }
        assertNotNull(analysis, "Analysis should not be null");
        assertNotNull(analysis.word(), "Word should not be null");
        assertNotNull(analysis.pinyin(), "Pinyin should not be null");
        assertNotNull(analysis.definition(), "Definition should not be null");
    }
}