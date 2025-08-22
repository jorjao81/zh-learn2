Feature: Domain Model Creation
  As a developer using the domain models
  I want to create domain objects with valid data
  So that I can ensure proper validation and business rules

  Scenario: Create a valid Chinese word
    When I create a ChineseWord with characters "你好"
    Then the ChineseWord should be created successfully
    And the ChineseWord characters should be "你好"

  Scenario: Reject Chinese word with null characters
    When I try to create a ChineseWord with null characters
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Chinese word characters cannot be null or empty"

  Scenario: Reject Chinese word with empty characters
    When I try to create a ChineseWord with characters ""
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Chinese word characters cannot be null or empty"

  Scenario: Reject Chinese word with whitespace-only characters
    When I try to create a ChineseWord with characters "   "
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Chinese word characters cannot be null or empty"

  Scenario: Create a valid Pinyin
    When I create a Pinyin with value "nǐ hǎo"
    Then the Pinyin should be created successfully
    And the Pinyin value should be "nǐ hǎo"

  Scenario: Reject Pinyin with null value
    When I try to create a Pinyin with null value
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Pinyin cannot be null or empty"

  Scenario: Reject Pinyin with empty value
    When I try to create a Pinyin with value ""
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Pinyin cannot be null or empty"

  Scenario: Create a valid Definition
    When I create a Definition with meaning "hello" and partOfSpeech "interjection"
    Then the Definition should be created successfully
    And the Definition meaning should be "hello"
    And the Definition partOfSpeech should be "interjection"

  Scenario: Reject Definition with null meaning
    When I try to create a Definition with null meaning and partOfSpeech "noun"
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Definition meaning cannot be null or empty"

  Scenario: Reject Definition with empty meaning
    When I try to create a Definition with meaning "" and partOfSpeech "noun"
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Definition meaning cannot be null or empty"

  Scenario: Reject Definition with null partOfSpeech
    When I try to create a Definition with meaning "hello" and null partOfSpeech
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Part of speech cannot be null or empty"

  Scenario: Reject Definition with empty partOfSpeech
    When I try to create a Definition with meaning "hello" and partOfSpeech ""
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Part of speech cannot be null or empty"

  Scenario: Create a valid Explanation
    When I create an Explanation with text "This greeting is commonly used in daily conversations"
    Then the Explanation should be created successfully
    And the Explanation text should be "This greeting is commonly used in daily conversations"

  Scenario: Reject Explanation with null text
    When I try to create an Explanation with null text
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Explanation cannot be null or empty"

  Scenario: Create a valid StructuralDecomposition
    When I create a StructuralDecomposition with text "你 (you) + 好 (good)"
    Then the StructuralDecomposition should be created successfully
    And the StructuralDecomposition text should be "你 (you) + 好 (good)"

  Scenario: Reject StructuralDecomposition with null text
    When I try to create a StructuralDecomposition with null text
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Structural decomposition cannot be null or empty"

  Scenario: Create a valid Example with usages
    When I create an Example with valid usages
    Then the Example should be created successfully
    And the Example should contain the provided usages

  Scenario: Reject Example with null usages
    When I try to create an Example with null usages
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Example usages cannot be null"

  Scenario: Create a valid Usage
    When I create a Usage with sentence "你好世界", pinyin "nǐ hǎo shì jiè", translation "Hello world", and context "greeting"
    Then the Usage should be created successfully
    And the Usage sentence should be "你好世界"
    And the Usage translation should be "Hello world"

  Scenario: Reject Usage with null sentence
    When I try to create a Usage with null sentence
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Usage sentence cannot be null or empty"

  Scenario: Reject Usage with null translation
    When I try to create a Usage with sentence "你好" and null translation
    Then an IllegalArgumentException should be thrown
    And the error message should contain "Usage translation cannot be null or empty"