package com.zhlearn.domain.model;

import com.zhlearn.domain.model.exercises.UsageDistinctionExercise;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UsageDistinctionExerciseStepDefinitions {
    
    private String sentence;
    private List<AnswerOption> answerOptions = new ArrayList<>();
    private UsageDistinctionExercise exercise;
    private Exception thrownException;
    private String result;
    private List<String> selectedAnswers = new ArrayList<>();
    
    @Given("the exercise:")
    public void the_exercise(DataTable dataTable) {
        Map<String, String> exerciseData = dataTable.asMap(String.class, String.class);
        sentence = exerciseData.get("sentence");
    }
    
    @Given("answer options:")
    public void answer_options(DataTable dataTable) {
        answerOptions.clear();
        List<Map<String, String>> options = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> option : options) {
            String optionText = option.get("option");
            boolean correct = Boolean.parseBoolean(option.get("correct"));
            String explanation = option.get("explanation");
            answerOptions.add(new AnswerOption(optionText, correct, explanation));
        }
    }
    
    @When("I attempt to create a usage distinction exercise")
    public void i_attempt_to_create_a_usage_distinction_exercise() {
        try {
            exercise = new UsageDistinctionExercise(sentence, answerOptions);
        } catch (Exception e) {
            thrownException = e;
        }
    }
    
    @When("a learner selects {string}")
    public void a_learner_selects(String answer) {
        selectedAnswers.clear();
        selectedAnswers.add(answer);
        result = exercise.evaluateAnswer(selectedAnswers);
    }
    
    @When("a learner selects {string}, {string}")
    public void a_learner_selects_two_answers(String answer1, String answer2) {
        selectedAnswers.clear();
        selectedAnswers.add(answer1);
        selectedAnswers.add(answer2);
        result = exercise.evaluateAnswer(selectedAnswers);
    }
    
    @When("a learner selects {string}, {string}, {string}")
    public void a_learner_selects_three_answers(String answer1, String answer2, String answer3) {
        selectedAnswers.clear();
        selectedAnswers.add(answer1);
        selectedAnswers.add(answer2);
        selectedAnswers.add(answer3);
        result = exercise.evaluateAnswer(selectedAnswers);
    }
    
    @When("a learner selects {string}, {string}, {string}, {string}")
    public void a_learner_selects_four_answers(String answer1, String answer2, String answer3, String answer4) {
        selectedAnswers.clear();
        selectedAnswers.add(answer1);
        selectedAnswers.add(answer2);
        selectedAnswers.add(answer3);
        selectedAnswers.add(answer4);
        result = exercise.evaluateAnswer(selectedAnswers);
    }
    
    @When("a learner selects no answers")
    public void a_learner_selects_no_answers() {
        selectedAnswers.clear();
        result = exercise.evaluateAnswer(selectedAnswers);
    }
    
    @Then("the exercise creation should fail")
    public void the_exercise_creation_should_fail() {
        assertNotNull(thrownException, "Expected an exception to be thrown");
        assertNull(exercise, "Exercise should not have been created");
    }
    
    @Then("the error should indicate {string}")
    public void the_error_should_indicate(String expectedMessage) {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains(expectedMessage),
                "Expected error message to contain: " + expectedMessage + 
                ", but was: " + thrownException.getMessage());
    }
    
    @Then("the result should be correct")
    public void the_result_should_be_correct() {
        assertEquals("correct", result);
    }
    
    @Then("the result should be incorrect")
    public void the_result_should_be_incorrect() {
        assertEquals("incorrect", result);
    }
}