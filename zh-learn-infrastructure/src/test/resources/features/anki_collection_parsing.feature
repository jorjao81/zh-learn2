Feature: Anki Collection Parsing
  Parse the full Anki collection (Chinese.txt) and keep only Chinese note types.

  Scenario: Skip non-Chinese note types and headers
    Given an Anki collection file:
      """
      #separator:tab
      #html:true
      CustomType	cí	词	[sound:ci.mp3]	definition
      Chinese	xué	学	[sound:xue.mp3]	definition
      Chinese 2	xí	习	[sound:xi.mp3]	definition
      Other	y	x	[sound:z.mp3]	def
      """
    When I parse the collection
    Then the parser should return 2 collection notes
    And the first collection note pinyin should be "xué"
    And the second collection note pronunciation should be "[sound:xi.mp3]"

  Scenario: Handle empty pronunciation
    Given an Anki collection file:
      """
      Chinese	xué	学		def
      Chinese	xí	习	[sound:xi.mp3]	def
      """
    When I parse the collection
    Then the parser should return 2 collection notes
    And the first collection note pronunciation should be ""
    And the second collection note pronunciation should be "[sound:xi.mp3]"
