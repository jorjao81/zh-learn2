package com.zhlearn.cli;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

public class WordAnalysisStepDefinitions {

    private WordAnalysis analysis;
    private Exception lastException;
    private Hanzi analyzedWord;

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
            this.analyzedWord = new Hanzi(word);

            // Create dummy providers for testing
            PinyinProvider pinyinProvider = new DummyPinyinProvider();
            DefinitionProvider definitionProvider = new DummyDefinitionProvider();
            ExampleProvider exampleProvider = new DummyExampleProvider();
            ExplanationProvider explanationProvider = new DummyExplanationProvider();
            StructuralDecompositionProvider decompositionProvider = new DummyStructuralDecompositionProvider();
            AudioProvider audioProvider = new DummyAudioProvider();

            // Create a basic analysis
            this.analysis = new WordAnalysis(
                analyzedWord,
                pinyinProvider.getPinyin(analyzedWord),
                definitionProvider.getDefinition(analyzedWord),
                decompositionProvider.getStructuralDecomposition(analyzedWord),
                exampleProvider.getExamples(analyzedWord, java.util.Optional.empty()),
                explanationProvider.getExplanation(analyzedWord),
                audioProvider.getPronunciation(analyzedWord, pinyinProvider.getPinyin(analyzedWord))
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

    @Given("I have a multi-character word {string}")
    public void i_have_a_multi_character_word(String word) {
        Hanzi hanzi = new Hanzi(word);

        assertTrue(hanzi.isMultiCharacter(), "Expected a multi-character word");
        this.analyzedWord = hanzi;
    }

    @Then("the response should contain sentence examples")
    public void the_response_should_contain_sentence_examples() {
        assertNotNull(analysis, "Analysis should have been performed");
        assertFalse(analysis.examples().usages().isEmpty(), "Expected at least one example usage");
    }

    @Then("the structural decomposition should show compound components")
    public void the_structural_decomposition_should_show_compound_components() {
        assertNotNull(analysis, "Analysis should have been performed");
        String decompositionText = analysis.structuralDecomposition().text();
        assertNotNull(decompositionText, "Structural decomposition text should not be null");
        assertTrue(decompositionText.toLowerCase().contains("component"),
            "Expected compound component details in structural decomposition");
    }
}
