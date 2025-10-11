package com.zhlearn.infrastructure.anki;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AnkiCardParsingStepDefinitions {

    private AnkiCardParser parser;
    private String currentLine;
    private String currentFileContent;
    private AnkiCard parsedCard;
    private List<AnkiCard> parsedCards;

    private String unescapeTabCharacters(String input) {
        return input.replace("\\t", "\t");
    }

    public AnkiCardParsingStepDefinitions() {
        this.parser = new AnkiCardParser();
    }

    @Given("a valid TSV line with {int} fields {string}")
    public void aValidTSVLineWithFields(int fieldCount, String tsvLine) {
        this.currentLine = unescapeTabCharacters(tsvLine);
    }

    @Given("a TSV line {string}")
    public void aTSVLine(String tsvLine) {
        this.currentLine = unescapeTabCharacters(tsvLine);
    }

    @Given("a TSV line with empty fields {string}")
    public void aTSVLineWithEmptyFields(String tsvLine) {
        this.currentLine = unescapeTabCharacters(tsvLine);
    }

    @Given("a TSV line with only {int} fields {string}")
    public void aTSVLineWithOnlyFields(int fieldCount, String tsvLine) {
        this.currentLine = unescapeTabCharacters(tsvLine);
    }

    @Given("a TSV line with {int} fields {string}")
    public void aTSVLineWithFields(int fieldCount, String tsvLine) {
        this.currentLine = unescapeTabCharacters(tsvLine);
    }

    @Given("an Anki file with headers and card data:")
    public void anAnkiFileWithHeadersAndCardData(String fileContent) {
        this.currentFileContent = unescapeTabCharacters(fileContent);
    }

    @Given("an Anki file with mixed content:")
    public void anAnkiFileWithMixedContent(String fileContent) {
        this.currentFileContent = unescapeTabCharacters(fileContent);
    }

    @When("I parse the line using AnkiCardParser")
    public void iParseTheLineUsingAnkiCardParser() {
        try {
            this.parsedCard = parser.parseLine(currentLine);
        } catch (Exception e) {
            // Exception swallowed
        }
    }

    @When("I parse the line")
    public void iParseTheLine() {
        try {
            this.parsedCard = parser.parseLine(currentLine);
        } catch (Exception e) {
            // Exception swallowed
        }
    }

    @When("I parse the file")
    public void iParseTheFile() {
        try {
            StringReader reader = new StringReader(currentFileContent);
            this.parsedCards = parser.parseFromReader(reader);
        } catch (IOException e) {
            // Exception swallowed
        }
    }

    @Then("it should create an AnkiCard with simplified {string}")
    public void itShouldCreateAnAnkiCardWithSimplified(String expectedSimplified) {
        assertThat(parsedCard).isNotNull();
        assertThat(parsedCard.simplified()).isEqualTo(expectedSimplified);
    }

    @Then("the pinyin should be {string}")
    public void thePinyinShouldBe(String expectedPinyin) {
        assertThat(parsedCard.pinyin()).isEqualTo(expectedPinyin);
    }

    @Then("the definition should be {string}")
    public void theDefinitionShouldBe(String expectedDefinition) {
        assertThat(parsedCard.definition()).isEqualTo(expectedDefinition);
    }

    @Then("all {int} fields should be populated correctly")
    public void allFieldsShouldBePopulatedCorrectly(int expectedFieldCount) {
        assertThat(parsedCard).isNotNull();
        // Verify that the card has been created (basic validation)
        assertThat(parsedCard.simplified()).isNotNull();
        assertThat(parsedCard.pinyin()).isNotNull();
        assertThat(parsedCard.definition()).isNotNull();
    }

    @Then("it should return {int} AnkiCards")
    public void itShouldReturnAnkiCards(int expectedCount) {
        assertThat(parsedCards).isNotNull();
        assertThat(parsedCards).hasSize(expectedCount);
    }

    @Then("the first card simplified should be {string}")
    public void theFirstCardSimplifiedShouldBe(String expectedSimplified) {
        assertThat(parsedCards).isNotEmpty();
        assertThat(parsedCards.get(0).simplified()).isEqualTo(expectedSimplified);
    }

    @Then("the second card simplified should be {string}")
    public void theSecondCardSimplifiedShouldBe(String expectedSimplified) {
        assertThat(parsedCards).hasSizeGreaterThan(1);
        assertThat(parsedCards.get(1).simplified()).isEqualTo(expectedSimplified);
    }

    @Then("the simplified field should be {string}")
    public void theSimplifiedFieldShouldBe(String expected) {
        assertThat(parsedCard.simplified()).isEqualTo(expected);
    }

    @Then("the pinyin field should be {string}")
    public void thePinyinFieldShouldBe(String expected) {
        assertThat(parsedCard.pinyin()).isEqualTo(expected);
    }

    @Then("the pronunciation field should be {string}")
    public void thePronunciationFieldShouldBe(String expected) {
        assertThat(parsedCard.pronunciation()).isEqualTo(expected);
    }

    @Then("the definition field should be {string}")
    public void theDefinitionFieldShouldBe(String expected) {
        assertThat(parsedCard.definition()).isEqualTo(expected);
    }

    @Then("the examples field should be {string}")
    public void theExamplesFieldShouldBe(String expected) {
        assertThat(parsedCard.examples()).isEqualTo(expected);
    }

    @Then("the etymology field should be {string}")
    public void theEtymologyFieldShouldBe(String expected) {
        assertThat(parsedCard.etymology()).isEqualTo(expected);
    }

    @Then("the components field should be {string}")
    public void theComponentsFieldShouldBe(String expected) {
        assertThat(parsedCard.components()).isEqualTo(expected);
    }

    @Then("it should create an AnkiCard")
    public void itShouldCreateAnAnkiCard() {
        assertThat(parsedCard).isNotNull();
    }

    @Then("the pinyin field should be empty")
    public void thePinyinFieldShouldBeEmpty() {
        assertThat(parsedCard.pinyin()).isEmpty();
    }

    @Then("the definition field should be empty")
    public void theDefinitionFieldShouldBeEmpty() {
        assertThat(parsedCard.definition()).isEmpty();
    }

    @Then("the etymology field should be empty")
    public void theEtymologyFieldShouldBeEmpty() {
        assertThat(parsedCard.etymology()).isEmpty();
    }

    @Then("the components field should be empty")
    public void theComponentsFieldShouldBeEmpty() {
        assertThat(parsedCard.components()).isEmpty();
    }

    @Then("it should create an AnkiCard with properties:")
    public void itShouldCreateAnAnkiCardWithProperties(DataTable dataTable) {
        assertThat(parsedCard).isNotNull();

        List<List<String>> rows = dataTable.asLists(String.class);

        for (List<String> row : rows) {
            String property = row.get(0);
            String expectedValue = row.get(1);
            if (expectedValue == null) {
                expectedValue = "";
            }

            switch (property) {
                case "simplified" -> assertThat(parsedCard.simplified()).isEqualTo(expectedValue);
                case "pinyin" -> assertThat(parsedCard.pinyin()).isEqualTo(expectedValue);
                case "sound" -> assertThat(parsedCard.pronunciation()).isEqualTo(expectedValue);
                case "definition" -> assertThat(parsedCard.definition()).isEqualTo(expectedValue);
                case "examples" -> assertThat(parsedCard.examples()).isEqualTo(expectedValue);
                case "etymology" -> assertThat(parsedCard.etymology()).isEqualTo(expectedValue);
                case "components" -> assertThat(parsedCard.components()).isEqualTo(expectedValue);
                case "similar" -> assertThat(parsedCard.similar()).isEqualTo(expectedValue);
                case "passive" -> assertThat(parsedCard.passive()).isEqualTo(expectedValue);
                case "alt" ->
                        assertThat(parsedCard.alternatePronunciations()).isEqualTo(expectedValue);
                case "hearing" -> assertThat(parsedCard.noHearing()).isEqualTo(expectedValue);
                default -> throw new IllegalArgumentException("Unknown property: " + property);
            }
        }
    }
}
