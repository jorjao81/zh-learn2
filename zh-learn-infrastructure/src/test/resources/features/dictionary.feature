Feature: Dictionary lookup functionality
  As a developer
  I want to look up Chinese words using a Dictionary
  So that I can get complete word analysis from AnkiCard data

  Scenario: Successfully lookup a Chinese word that exists in the dictionary
    Given I have a Dictionary from AnkiCard data:
      | simplified | pinyin | definition     | components | examples     | etymology    |
      | 汉语         | hànyǔ  | Chinese language 1 | 语         | 你好         | <etymology>  |
      | 你好         | nǐ hǎo | Hello          | 你,好      | 你好世界     | <etymology>  |
    When I lookup the word "汉语"
    Then the lookup should return a WordAnalysis with properties:
      | simplified              | 汉语            |
      | pinyin                  | hànyǔ           |
      | definition              | Chinese language 1 |
      | structural_decomposition | 语        |
      | examples            | 你好            |
      | explanation         | <etymology>             |

  Scenario: Lookup a Chinese word that does not exist in the dictionary
    Given I have a Dictionary from AnkiCard data:
      | simplified | pinyin | definition     | components | examples     | etymology    |
      | 汉语         | hànyǔ  | Chinese language | 语         | 你好         | <etymology>  |
    When I lookup the word "不存在"
    Then the lookup should return empty

  Scenario: Lookup with null or empty input
    Given I have a Dictionary from AnkiCard data:
      | simplified | pinyin | definition     | components | examples     | etymology    |
      | 汉语         | hànyǔ  | Chinese language | 语         | 你好         | <etymology>  |
    When I lookup the word ""
    Then the lookup should return empty
    When I lookup a null word
    Then the lookup should return empty

  Scenario: Dictionary providers delegate to dictionary lookup
    Given I have a Dictionary from AnkiCard data:
      | simplified | pinyin | definition     | components | examples     | etymology    |
      | 汉语         | hànyǔ  | Chinese language | 汉         | 你好         | <etymology>  |
    When I request pinyin for "汉语" from DictionaryPinyinProvider
    Then the provider should return pinyin "hànyǔ"
    When I request definition for "汉语" from DictionaryDefinitionProvider
    Then the provider should return definition "Chinese language"

  Scenario: Dictionary providers return empty for non-existent words
    Given I have a Dictionary from AnkiCard data:
      | simplified | pinyin | definition     | components | examples     | etymology    |
      | 汉语         | hànyǔ  | Chinese language | 语         | 你好         | <etymology>  |
    When I request pinyin for "不存在" from DictionaryPinyinProvider
    And I request definition for "不存在" from DictionaryDefinitionProvider
    Then the provider should return empty pinyin
    And the provider should return empty definition

  Scenario: Dictionary handles AnkiCard with missing fields
    Given I have a Dictionary from AnkiCard data:
      | simplified | pinyin | definition     | components | examples     | etymology    |
      | 汉语         | hànyǔ  | Chinese language | 语         | 你好         |              |
    When I lookup the word "汉语"
    Then the lookup should return empty

