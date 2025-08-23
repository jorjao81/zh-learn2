package com.zhlearn.infrastructure.anki;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.provider.DefinitionProvider;
import com.zhlearn.domain.provider.PinyinProvider;
import com.zhlearn.infrastructure.dictionary.AnkiCardDictionary;
import com.zhlearn.infrastructure.dictionary.DictionaryDefinitionProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryPinyinProvider;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DictionaryStepDefs {

    private Dictionary dictionary;
    private Optional<WordAnalysis> wordAnalysis;
    private Optional<Pinyin> pinyin;
    private Optional<Definition> definition;
    private List<AnkiCard> ankiCards;
    private Optional<Pinyin> providerReturn;

    @Given("I have a Dictionary from AnkiCard data:")
    public void i_have_the_following_anki_card_data(DataTable dataTable) throws Exception {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        ankiCards = rows.stream()
                .map(row -> AnkiCard.of(
                        row.get("simplified"),
                        row.get("pinyin"),
                        row.get("pronunciation"),
                        row.get("definition"),
                        row.get("examples"),
                        row.get("etymology"),
                        row.get("components"),
                        row.get("similar"),
                        row.get("similar"),
                        row.get("passive"),
                        row.get("alternatePronunciations"),
                        row.get("noHearing"))).toList();
        dictionary = new AnkiCardDictionary(ankiCards);
    }

    @When("I lookup the word {string}")
    public void i_lookup_the_word(String word) {
        wordAnalysis = dictionary.lookup(word);
    }

    @Then("the lookup should return a WordAnalysis")
    public void the_lookup_should_return_a_word_analysis() {
        assertTrue(wordAnalysis.isPresent());
    }

    @Then("the analysis should have simplified characters {string}")
    public void the_analysis_should_have_simplified_characters(String simplified) {
        assertEquals(simplified, wordAnalysis.get().word().characters());
    }

    @Then("the analysis should have pinyin")
    public void the_analysis_should_have_pinyin() {
        assertNotNull(wordAnalysis.get().pinyin());
    }

    @Then("the analysis should have definition")
    public void the_analysis_should_have_definition() {
        assertNotNull(wordAnalysis.get().definition());
    }

    @Then("the analysis should have structural decomposition")
    public void the_analysis_should_have_structural_decomposition() {
        assertNotNull(wordAnalysis.get().structuralDecomposition());
    }

    @Then("the analysis should have examples")
    public void the_analysis_should_have_examples() {
        assertNotNull(wordAnalysis.get().examples());
    }

    @Then("the analysis should have explanation")
    public void the_analysis_should_have_explanation() {
        assertNotNull(wordAnalysis.get().explanation());
    }

    @Then("the lookup should return empty")
    public void the_lookup_should_return_empty() {
        assertTrue(wordAnalysis.isEmpty());
    }

    @When("I lookup a null word")
    public void i_lookup_a_null_word() {
        wordAnalysis = dictionary.lookup(null);
    }

    @When("I request pinyin for {string} from DictionaryPinyinProvider")
    public void i_request_pinyin_for_from_dictionary_pinyin_provider(String word) {
        PinyinProvider pinyinProvider = new DictionaryPinyinProvider(dictionary);
        pinyin = Optional.ofNullable(pinyinProvider.getPinyin(new Hanzi(word)));
    }


    @When("I request definition for {string} from DictionaryDefinitionProvider")
    public void iRequestDefinitionForFromDictionaryDefinitionProvider(String word) {
        DefinitionProvider definitionProvider = new DictionaryDefinitionProvider(dictionary);
        definition = Optional.ofNullable(definitionProvider.getDefinition(new Hanzi(word)));
    }

    @Then("the provider should return pinyin {string}")
    public void theProviderShouldReturn(String arg0) {
        assertThat(pinyin).isPresent();
        assertThat(pinyin.get().pinyin()).isEqualTo(arg0);
    }


    @Then("the provider should return empty pinyin")
    public void the_provider_should_return_empty_pinyin() {
        assertThat(pinyin).isEmpty();
    }

    @Then("the provider should return definition {string}")
    public void the_provider_should_return_definition(String arg0) {
        assertThat(definition).isPresent();
        assertThat(definition.get().meaning()).isEqualTo(arg0);
    }

    @Then("the provider should return empty definition")
    public void the_provider_should_return_empty_definition() {
        assertThat(definition).isEmpty();
    }

    @Then("the lookup should return a WordAnalysis with properties:")
    public void the_lookup_should_return_a_word_analysis_with_properties(DataTable dataTable) {
        assertTrue(wordAnalysis.isPresent());
        
        WordAnalysis analysis = wordAnalysis.get();
        List<List<String>> rows = dataTable.asLists(String.class);
        
        for (List<String> row : rows) {
            String property = row.get(0);
            String expectedValue = row.get(1);
            
            switch (property) {
                case "simplified" -> assertEquals(expectedValue, analysis.word().characters());
                case "pinyin" -> assertEquals(expectedValue, analysis.pinyin().pinyin());
                case "definition" -> assertEquals(expectedValue, analysis.definition().meaning());
                case "structural_decomposition" -> assertEquals(expectedValue, analysis.structuralDecomposition().decomposition());
                case "explanation" -> assertEquals(expectedValue, analysis.explanation().explanation());
                // TODO: fix examples
                case "examples" -> assertEquals(expectedValue, analysis.examples().usages().get(0).sentence());
                default -> fail("Unknown property: " + property);
            }
        }
    }

}