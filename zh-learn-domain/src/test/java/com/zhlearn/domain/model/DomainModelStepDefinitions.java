package com.zhlearn.domain.model;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DomainModelStepDefinitions {
    
    private Hanzi hanzi;
    private Pinyin pinyin;
    private Definition definition;
    private Explanation explanation;
    private StructuralDecomposition structuralDecomposition;
    private Example example;
    private Example.Usage usage;
    
    private Exception thrownException;

    // ChineseWord step definitions
    @When("I create a ChineseWord with characters {string}")
    public void i_create_a_chinese_word_with_characters(String characters) {
        hanzi = new Hanzi(characters);
    }

    @When("I try to create a ChineseWord with null characters")
    public void i_try_to_create_a_chinese_word_with_null_characters() {
        try {
            hanzi = new Hanzi(null);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I try to create a ChineseWord with characters {string}")
    public void i_try_to_create_a_chinese_word_with_characters(String characters) {
        try {
            hanzi = new Hanzi(characters);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the ChineseWord should be created successfully")
    public void the_chinese_word_should_be_created_successfully() {
        assertNotNull(hanzi);
    }

    @Then("the ChineseWord characters should be {string}")
    public void the_chinese_word_characters_should_be(String expectedCharacters) {
        assertEquals(expectedCharacters, hanzi.characters());
    }

    // Pinyin step definitions
    @When("I create a Pinyin with value {string}")
    public void i_create_a_pinyin_with_value(String value) {
        pinyin = new Pinyin(value);
    }

    @When("I try to create a Pinyin with null value")
    public void i_try_to_create_a_pinyin_with_null_value() {
        try {
            pinyin = new Pinyin(null);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I try to create a Pinyin with value {string}")
    public void i_try_to_create_a_pinyin_with_value(String value) {
        try {
            pinyin = new Pinyin(value);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the Pinyin should be created successfully")
    public void the_pinyin_should_be_created_successfully() {
        assertNotNull(pinyin);
    }

    @Then("the Pinyin value should be {string}")
    public void the_pinyin_value_should_be(String expectedValue) {
        assertEquals(expectedValue, pinyin.pinyin());
    }

    // Definition step definitions
    @When("I create a Definition with meaning {string}")
    public void i_create_a_definition_with_meaning(String meaning) {
        definition = new Definition(meaning);
    }

    @When("I try to create a Definition with null meaning")
    public void i_try_to_create_a_definition_with_null_meaning() {
        try {
            definition = new Definition(null);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I try to create a Definition with meaning {string}")
    public void i_try_to_create_a_definition_with_meaning(String meaning) {
        try {
            definition = new Definition(meaning);
        } catch (Exception e) {
            thrownException = e;
        }
    }


    @Then("the Definition should be created successfully")
    public void the_definition_should_be_created_successfully() {
        assertNotNull(definition);
    }

    @Then("the Definition meaning should be {string}")
    public void the_definition_meaning_should_be(String expectedMeaning) {
        assertEquals(expectedMeaning, definition.meaning());
    }


    // Explanation step definitions
    @When("I create an Explanation with text {string}")
    public void i_create_an_explanation_with_text(String text) {
        explanation = new Explanation(text);
    }

    @When("I try to create an Explanation with null text")
    public void i_try_to_create_an_explanation_with_null_text() {
        try {
            explanation = new Explanation(null);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the Explanation should be created successfully")
    public void the_explanation_should_be_created_successfully() {
        assertNotNull(explanation);
    }

    @Then("the Explanation text should be {string}")
    public void the_explanation_text_should_be(String expectedText) {
        assertEquals(expectedText, explanation.explanation());
    }

    // StructuralDecomposition step definitions
    @When("I create a StructuralDecomposition with text {string}")
    public void i_create_a_structural_decomposition_with_text(String text) {
        structuralDecomposition = new StructuralDecomposition(text);
    }

    @When("I try to create a StructuralDecomposition with null text")
    public void i_try_to_create_a_structural_decomposition_with_null_text() {
        try {
            structuralDecomposition = new StructuralDecomposition(null);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the StructuralDecomposition should be created successfully")
    public void the_structural_decomposition_should_be_created_successfully() {
        assertNotNull(structuralDecomposition);
    }

    @Then("the StructuralDecomposition text should be {string}")
    public void the_structural_decomposition_text_should_be(String expectedText) {
        assertEquals(expectedText, structuralDecomposition.decomposition());
    }

    // Example step definitions
    @When("I create an Example with valid usages")
    public void i_create_an_example_with_valid_usages() {
        Example.Usage usage1 = new Example.Usage("你好", "nǐ hǎo", "hello", "greeting", "Test breakdown for hello");
        Example.Usage usage2 = new Example.Usage("再见", "zài jiàn", "goodbye", "parting", "Test breakdown for goodbye");
        example = new Example(List.of(usage1, usage2), List.of());
    }

    @When("I try to create an Example with null usages")
    public void i_try_to_create_an_example_with_null_usages() {
        try {
            example = new Example(null, List.of());
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the Example should be created successfully")
    public void the_example_should_be_created_successfully() {
        assertNotNull(example);
    }

    @Then("the Example should contain the provided usages")
    public void the_example_should_contain_the_provided_usages() {
        assertNotNull(example.usages());
        assertEquals(2, example.usages().size());
        assertEquals("你好", example.usages().get(0).sentence());
        assertEquals("再见", example.usages().get(1).sentence());
    }

    // Usage step definitions
    @When("I create a Usage with sentence {string}, pinyin {string}, translation {string}, and context {string}")
    public void i_create_a_usage_with_sentence_pinyin_translation_and_context(
            String sentence, String pinyin, String translation, String context) {
        usage = new Example.Usage(sentence, pinyin, translation, context, "Test breakdown");
    }

    @When("I try to create a Usage with null sentence")
    public void i_try_to_create_a_usage_with_null_sentence() {
        try {
            usage = new Example.Usage(null, "pinyin", "translation", "context", "breakdown");
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("I try to create a Usage with sentence {string} and null translation")
    public void i_try_to_create_a_usage_with_sentence_and_null_translation(String sentence) {
        try {
            usage = new Example.Usage(sentence, "pinyin", null, "context", "breakdown");
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the Usage should be created successfully")
    public void the_usage_should_be_created_successfully() {
        assertNotNull(usage);
    }

    @Then("the Usage sentence should be {string}")
    public void the_usage_sentence_should_be(String expectedSentence) {
        assertEquals(expectedSentence, usage.sentence());
    }

    @Then("the Usage translation should be {string}")
    public void the_usage_translation_should_be(String expectedTranslation) {
        assertEquals(expectedTranslation, usage.translation());
    }

    // Common step definitions for exception handling
    @Then("an IllegalArgumentException should be thrown")
    public void an_illegal_argument_exception_should_be_thrown() {
        assertNotNull(thrownException);
        assertInstanceOf(IllegalArgumentException.class, thrownException);
    }

    @Then("the error message should contain {string}")
    public void the_error_message_should_contain(String expectedMessage) {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains(expectedMessage));
    }
}
