Feature: Anki Card Parsing
  As a system that needs to import Anki cards
  I want to parse TSV formatted Anki cards
  So that I can extract structured data from external Anki files

  Scenario: Parse valid single Anki card line
    Given a valid TSV line with 11 fields "闯\tchuǎng\t[sound:file.mp3]\tto rush\texamples\tetymology\tcomponents\tsimilar\tpassive\talt\thearing"
    When I parse the line using AnkiCardParser
    Then it should create an AnkiCard with properties:
      | simplified  | 闯               |
      | pinyin      | chuǎng           |
      | sound       | [sound:file.mp3] |
      | definition  | to rush          |
      | examples    | examples         |
      | etymology   | etymology        |
      | components  | components       |
      | similar     | similar          |
      | passive     | passive          |
      | alt         | alt              |
      | hearing     | hearing          |

  Scenario: Parse file with headers and multiple cards
    Given an Anki file with headers and card data:
      """
      #separator:tab
      #html:true
      #notetype column:1
      #tags column:13
      闯\tchuǎng\t[sound:file1.mp3]\tto rush\texamples1\tetymology1\tcomponents1\tsimilar1\tpassive1\talt1\thearing1
      旋\txuán\t[sound:file2.mp3]\tto turn\texamples2\tetymology2\tcomponents2\tsimilar2\tpassive2\talt2\thearing2
      """
    When I parse the file
    Then it should return 2 AnkiCards
    And the first card simplified should be "闯"
    And the second card simplified should be "旋"

  Scenario: Skip header lines starting with #
    Given an Anki file with mixed content:
      """
      #separator:tab
      #html:true
      闯\tchuǎng\t[sound:file.mp3]\tto rush\texamples\tetymology\tcomponents\tsimilar\tpassive\talt\thearing
      #comment line
      旋\txuán\t[sound:file2.mp3]\tto turn\texamples2\tetymology2\tcomponents2\tsimilar2\tpassive2\talt2\thearing2
      """
    When I parse the file
    Then it should return 2 AnkiCards

  Scenario: Handle empty fields gracefully
    Given a TSV line with empty fields "闯\t\t[sound:file.mp3]\t\texamples\t\t\tsimilar\t\t\thearing"
    When I parse the line
    Then it should create an AnkiCard with properties:
      | simplified  | 闯               |
      | pinyin      |                  |
      | sound       | [sound:file.mp3] |
      | definition  |                  |
      | examples    | examples         |
      | etymology   |                  |
      | components  |                  |
      | similar     | similar          |
      | passive     |                  |
      | alt         |                  |
      | hearing     | hearing          |

  Scenario: Handle missing columns by padding with empty strings
    Given a TSV line with only 5 fields "闯\tchuǎng\t[sound:file.mp3]\tto rush\texamples"
    When I parse the line
    Then it should create an AnkiCard with properties:
      | simplified  | 闯               |
      | pinyin      | chuǎng           |
      | sound       | [sound:file.mp3] |
      | definition  | to rush          |
      | examples    | examples         |
      | etymology   |                  |
      | components  |                  |
      | similar     |                  |
      | passive     |                  |
      | alt         |                  |
      | hearing     |                  |

  Scenario: Handle extra columns by ignoring them
    Given a TSV line with 15 fields "闯\tchuǎng\t[sound:file.mp3]\tto rush\texamples\tetymology\tcomponents\tsimilar\tpassive\talt\thearing\textra1\textra2\textra3\textra4"
    When I parse the line
    And the simplified field should be "闯"
