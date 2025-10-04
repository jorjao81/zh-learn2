Feature: CLI Help Command
  As a CLI user
  I want to run the help command
  So that I can see usage information

  Scenario: Running help command
    When I run the CLI with "--help"
    Then the exit code should be 0
    And the output should contain "Usage:"
