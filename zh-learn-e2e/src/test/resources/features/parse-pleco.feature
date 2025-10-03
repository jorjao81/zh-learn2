Feature: Parse Pleco Command
  As a CLI user
  I want to process Pleco export files
  So that I can analyze Chinese vocabulary and export to Anki

  Scenario: Processing Pleco export with OpenRouter provider
    Given I have a Pleco export file with content:
      """
      秽	hui4	(orig.) to be overrun with weeds → weeds ⇒ dirty, filthy => debauchery
      液态	ye4tai4	noun liquid state; liquidness
      """
    When I run parse-pleco with parameters "--explanation-provider=openrouter --definition-formatter-provider=openrouter --example-provider=openrouter --decomposition-provider=openrouter --parallel-threads=10 --model=google/gemma-3n-e4b-it --skip-audio"
    Then the exit code should be 0
    And the Anki export file should exist
    And each word in the export should have an explanation with more than 500 characters
    And the definition of "秽" should be "(orig.) to be overrun with weeds → weeds ⇒ dirty, filthy => debauchery"
