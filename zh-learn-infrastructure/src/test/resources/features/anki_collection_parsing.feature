Feature: Anki Collection Parsing
  Parse the full Anki collection (Chinese.txt) and keep only Chinese note types.

  Scenario: Skip non-Chinese note types and headers
    Given an Anki collection file:
      """
      #separator:tab
      #html:true
      CustomType	词	cí	[sound:ci.mp3]	definition
      Chinese	学	xué	[sound:xue.mp3]	definition
      Chinese 2	习	xí	[sound:xi.mp3]	definition
      Other	x	y	[sound:z.mp3]	def
      """
    When I parse the collection
    Then the parser should return 2 collection notes
    And the first collection note pinyin should be "xué"
    And the second collection note pronunciation should be "[sound:xi.mp3]"

  Scenario: Handle empty pronunciation
    Given an Anki collection file:
      """
      Chinese	学	xué		def
      Chinese	习	xí	[sound:xi.mp3]	def
      """
    When I parse the collection
    Then the parser should return 2 collection notes
    And the first collection note pronunciation should be ""
    And the second collection note pronunciation should be "[sound:xi.mp3]"

