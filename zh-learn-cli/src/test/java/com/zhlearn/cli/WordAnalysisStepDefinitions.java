package com.zhlearn.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import com.zhlearn.domain.model.Example;
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

public class WordAnalysisStepDefinitions {

    private WordAnalysis analysis;
    private Exception lastException;
    private String requestedWord;

    @Given("the ZH Learn application is available")
    public void the_zh_learn_application_is_available() {
        // Application is always available in tests
        // Just verify we can create basic components
        assertNotNull(new Hanzi("你好"));
    }

    @Given("I have a multi-character word {string}")
    public void i_have_a_multi_character_word(String word) {
        Hanzi hanzi = new Hanzi(word);
        assertThat(hanzi.isMultiCharacter()).isTrue();
        this.requestedWord = word;
    }

    @When("I analyze the word {string} using provider {string}")
    public void i_analyze_the_word_using_provider(String word, String providerName) {
        this.requestedWord = word;
        performAnalysis(word, providerName);
    }

    @When("I analyze the word using {string} providers")
    public void i_analyze_the_word_using_providers(String providerName) {
        if (requestedWord == null) {
            throw new IllegalStateException("Word must be defined before analysis");
        }
        performAnalysis(requestedWord, providerName);
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

    @Then("the response should contain sentence examples")
    public void the_response_should_contain_sentence_examples() {
        assertNotNull(analysis, "Analysis should not be null");
        Example examples = analysis.examples();
        assertNotNull(examples, "Examples should not be null");
        assertThat(examples.usages()).isNotEmpty();
        String characters = analysis.word().characters();
        boolean containsWord =
                examples.usages().stream().anyMatch(usage -> usage.sentence().contains(characters));
        assertThat(containsWord).isTrue();
    }

    @Then("the structural decomposition should show compound components")
    public void the_structural_decomposition_should_show_compound_components() {
        assertNotNull(analysis, "Analysis should not be null");
        String decompositionText = analysis.structuralDecomposition().decomposition();
        assertThat(decompositionText).contains(analysis.word().characters());
        assertThat(decompositionText).contains(":");
    }

    private void performAnalysis(String word, String providerName) {
        try {
            Hanzi hanzi = new Hanzi(word);
            PinyinProvider pinyinProvider = new DummyPinyinProvider();
            DefinitionProvider definitionProvider = new DummyDefinitionProvider();
            ExampleProvider exampleProvider = new DummyExampleProvider();
            ExplanationProvider explanationProvider = new DummyExplanationProvider();
            StructuralDecompositionProvider decompositionProvider =
                    new DummyStructuralDecompositionProvider();
            AudioProvider audioProvider = new DummyAudioProvider();

            this.analysis =
                    new WordAnalysis(
                            hanzi,
                            pinyinProvider.getPinyin(hanzi),
                            definitionProvider.getDefinition(hanzi),
                            decompositionProvider.getStructuralDecomposition(hanzi),
                            exampleProvider.getExamples(hanzi, Optional.empty()),
                            explanationProvider.getExplanation(hanzi),
                            audioProvider.getPronunciation(hanzi, pinyinProvider.getPinyin(hanzi)));

            this.lastException = null;
        } catch (Exception e) {
            this.lastException = e;
            this.analysis = null;
        }
    }
}
