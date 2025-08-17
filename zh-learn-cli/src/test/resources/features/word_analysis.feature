Feature: Chinese Word Analysis
  As a Chinese language learner
  I want to analyze Chinese words using various AI providers
  So that I can understand their pinyin, meaning, structure, usage, and cultural context

  Background:
    Given the ZH Learn application is available

  Scenario: Analyze word with dummy provider
    When I analyze the word "汉语" using provider "dummy-pinyin"
    Then the analysis should contain pinyin information
    And the analysis should contain definition information
    And the analysis should contain structural decomposition
    And the analysis should contain usage examples
    And the analysis should contain explanation with etymology

  Scenario: Handle invalid provider
    When I try to analyze the word "汉语" using provider "non-existent-provider"
    Then an error should be thrown indicating the provider was not found

  Scenario: Handle empty word
    When I try to analyze an empty word using provider "dummy-pinyin"
    Then an error should be thrown indicating invalid input

  Scenario: Complete word analysis workflow
    Given I have a Chinese word "学习"
    When I perform a complete analysis using provider "dummy-definition"
    Then the result should include:
      | field                    | value                          |
      | word                     | 学习                           |
      | provider_name           | dummy-definition               |
      | pinyin_available        | true                           |
      | definition_available    | true                           |
      | decomposition_available | true                           |
      | examples_available      | true                           |
      | explanation_available   | true                           |

  Scenario Outline: Multiple provider analysis
    Given I have the word "<word>"
    When I analyze it with provider "<provider>"
    Then the analysis should be successful
    And the provider name should be "<provider>"

    Examples:
      | word | provider                |
      | 汉语 | dummy-pinyin           |
      | 学习 | dummy-definition       |
      | 中文 | dummy-example          |