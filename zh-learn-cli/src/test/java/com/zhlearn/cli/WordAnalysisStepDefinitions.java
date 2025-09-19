package com.zhlearn.cli;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class WordAnalysisStepDefinitions {
    
    private WordAnalysisServiceImpl wordAnalysisService;
    private Hanzi currentWord;
    private WordAnalysis currentAnalysis;
    private Exception lastException;
    
    @Given("the ZH Learn application is available")
    public void the_zh_learn_application_is_available() {
        MainCommand main = new MainCommand();
        main.addAudioProvider(new AbsolutePathAudioProvider());
        this.wordAnalysisService = main.getWordAnalysisService();
    }
    
    @When("I analyze the word {string} using provider {string}")
    public void i_analyze_the_word_using_provider(String word, String provider) {
        try {
            currentWord = new Hanzi(word);
            currentAnalysis = wordAnalysisService.getCompleteAnalysis(currentWord, provider);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @When("I try to analyze the word {string} using provider {string}")
    public void i_try_to_analyze_the_word_using_provider(String word, String provider) {
        try {
            currentWord = new Hanzi(word);
            currentAnalysis = wordAnalysisService.getCompleteAnalysis(currentWord, provider);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @When("I try to analyze an empty word using provider {string}")
    public void i_try_to_analyze_an_empty_word_using_provider(String provider) {
        try {
            currentWord = new Hanzi("");
            currentAnalysis = wordAnalysisService.getCompleteAnalysis(currentWord, provider);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @Given("I have a Chinese word {string}")
    public void i_have_a_chinese_word(String word) {
        currentWord = new Hanzi(word);
    }
    
    @When("I perform a complete analysis using provider {string}")
    public void i_perform_a_complete_analysis_using_provider(String provider) {
        try {
            currentAnalysis = wordAnalysisService.getCompleteAnalysis(currentWord, provider);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @Given("I have the word {string}")
    public void i_have_the_word(String word) {
        currentWord = new Hanzi(word);
    }
    
    @When("I analyze it with provider {string}")
    public void i_analyze_it_with_provider(String provider) {
        try {
            currentAnalysis = wordAnalysisService.getCompleteAnalysis(currentWord, provider);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @Then("the analysis should contain pinyin information")
    public void the_analysis_should_contain_pinyin_information() {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.pinyin());
        assertNotNull(currentAnalysis.pinyin().pinyin());
        assertFalse(currentAnalysis.pinyin().pinyin().trim().isEmpty());
    }
    
    @Then("the analysis should contain definition information")
    public void the_analysis_should_contain_definition_information() {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.definition());
        assertNotNull(currentAnalysis.definition().meaning());
        assertFalse(currentAnalysis.definition().meaning().trim().isEmpty());
    }
    
    @Then("the analysis should contain structural decomposition")
    public void the_analysis_should_contain_structural_decomposition() {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.structuralDecomposition());
        assertNotNull(currentAnalysis.structuralDecomposition().decomposition());
        assertFalse(currentAnalysis.structuralDecomposition().decomposition().trim().isEmpty());
    }
    
    @Then("the analysis should contain usage examples")
    public void the_analysis_should_contain_usage_examples() {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.examples());
        assertNotNull(currentAnalysis.examples().usages());
    }
    
    @Then("the analysis should contain explanation with etymology")
    public void the_analysis_should_contain_explanation_with_etymology() {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.explanation());
        assertNotNull(currentAnalysis.explanation().explanation());
        assertFalse(currentAnalysis.explanation().explanation().trim().isEmpty());
    }
    
    @Then("an error should be thrown indicating the provider was not found")
    public void an_error_should_be_thrown_indicating_the_provider_was_not_found() {
        assertNotNull(lastException);
        assertTrue(lastException.getMessage().contains("provider not found") || 
                  lastException.getMessage().contains("not found"));
    }
    
    @Then("an error should be thrown indicating invalid input")
    public void an_error_should_be_thrown_indicating_invalid_input() {
        assertNotNull(lastException);
        assertTrue(lastException.getMessage().contains("cannot be null or empty") ||
                  lastException.getMessage().contains("invalid"));
    }
    
    @Then("the result should include:")
    public void the_result_should_include(io.cucumber.datatable.DataTable dataTable) {
        assertNotNull(currentAnalysis);
        
        var rows = dataTable.asLists(String.class);
        for (var row : rows) {
            if (row.size() != 2) continue;

            String field = row.get(0);
            String expectedValue = row.get(1);

            switch (field) {
                case "word":
                    assertEquals(expectedValue, currentAnalysis.word().characters());
                    break;
                case "pinyin_available":
                    assertEquals(Boolean.parseBoolean(expectedValue), currentAnalysis.pinyin() != null);
                    break;
                case "definition_available":
                    assertEquals(Boolean.parseBoolean(expectedValue), currentAnalysis.definition() != null);
                    break;
                case "decomposition_available":
                    assertEquals(Boolean.parseBoolean(expectedValue), currentAnalysis.structuralDecomposition() != null);
                    break;
                case "examples_available":
                    assertEquals(Boolean.parseBoolean(expectedValue), currentAnalysis.examples() != null);
                    break;
                case "explanation_available":
                    assertEquals(Boolean.parseBoolean(expectedValue), currentAnalysis.explanation() != null);
                    break;
            }
        }
    }
    
    @Then("the analysis should be successful")
    public void the_analysis_should_be_successful() {
        assertNull(lastException);
        assertNotNull(currentAnalysis);
    }
    
    @Then("the pinyin should be {string}")
    public void the_pinyin_should_be(String expectedPinyin) {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.pinyin());
        assertEquals(expectedPinyin, currentAnalysis.pinyin().pinyin());
    }
    
    @Then("the definition meaning should be {string}")
    public void the_definition_meaning_should_be(String expectedMeaning) {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.definition());
        assertEquals(expectedMeaning, currentAnalysis.definition().meaning());
    }
    
    @Then("the structural decomposition should be {string}")
    public void the_structural_decomposition_should_be(String expectedDecomposition) {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.structuralDecomposition());
        assertEquals(expectedDecomposition, currentAnalysis.structuralDecomposition().decomposition());
    }
    
    @Then("the explanation should be {string}")
    public void the_explanation_should_be(String expectedExplanation) {
        assertNotNull(currentAnalysis);
        assertNotNull(currentAnalysis.explanation());
        assertEquals(expectedExplanation, currentAnalysis.explanation().explanation());
    }

    private static class AbsolutePathAudioProvider implements AudioProvider {
        private static final Path AUDIO = Path.of("src/test/resources/fixtures/audio/sample.mp3").toAbsolutePath();

        @Override
        public String getName() {
            return "test-audio";
        }

        @Override
        public String getDescription() {
            return "Test audio provider returning an absolute fixture path";
        }

        @Override
        public ProviderType getType() {
            return ProviderType.DUMMY;
        }

        @Override
        public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) {
            return Optional.of(AUDIO);
        }
    }
}
