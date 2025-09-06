package com.zhlearn.infrastructure.anki;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AnkiCollectionParsingStepDefinitions {

    private String content;
    private List<AnkiNote> notes;
    private Exception error;

    @Given("an Anki collection file:")
    public void anAnkiCollectionFile(String fileContent) {
        this.content = fileContent.replace("\\t", "\t");
    }

    @When("I parse the collection")
    public void iParseTheCollection() {
        try {
            AnkiNoteParser parser = new AnkiNoteParser();
            this.notes = parser.parseFromReader(new StringReader(content));
        } catch (IOException e) {
            this.error = e;
        }
    }

    @Then("the parser should return {int} collection notes")
    public void theParserShouldReturnCollectionNotes(int expected) {
        assertThat(error).isNull();
        assertThat(notes).isNotNull();
        assertThat(notes).hasSize(expected);
    }

    @Then("the first collection note pinyin should be {string}")
    public void theFirstCollectionNotePinyinShouldBe(String expected) {
        assertThat(notes).isNotEmpty();
        assertThat(notes.get(0).pinyin()).isEqualTo(expected);
    }

    @Then("the second collection note pronunciation should be {string}")
    public void theSecondCollectionNotePronunciationShouldBe(String expected) {
        assertThat(notes).hasSizeGreaterThan(1);
        assertThat(notes.get(1).pronunciation()).isEqualTo(expected);
    }

    @Then("the first collection note pronunciation should be {string}")
    public void theFirstCollectionNotePronunciationShouldBe(String expected) {
        assertThat(notes).isNotEmpty();
        assertThat(notes.get(0).pronunciation()).isEqualTo(expected);
    }
}
