# Chinese Words from characters

Generate word examples for the Chinese character: **{WORD}**
{CONTEXT}

Please provide examples grouped by different meanings or usages of this character.

## Output Format
Return your response as YAML in the following structure:

```yaml
response:
  - meaning: "description of meaning 1"
    pinyin: "pronunciation for this meaning"
    examples:
      - hanzi: "Chinese word containing the character"
        pinyin: "phonetic transcription"
        translation: "English translation"
        breakdown: "Explanation of how the character contributes to the word's meaning"
      - hanzi: "Another Chinese word containing the character"
        pinyin: "phonetic transcription" 
        translation: "English translation"
        breakdown: "Explanation of how the character contributes to the word's meaning"
  - meaning: "description of meaning 2 (if applicable)"
    pinyin: "pronunciation for this meaning"
    examples:
      - hanzi: "Chinese word for meaning 2"
        pinyin: "phonetic transcription"
        translation: "English translation"
        breakdown: "Explanation of how the character contributes to the word's meaning"
```



## Requirements
- Group examples by distinct meanings/usages of the character
- Include the pinyin pronunciation for each meaning group
- Provide 1-4 examples per meaning
- Include accurate pinyin with tone marks for each word
- Provide clear English translations
- Include breakdown explanations showing how the character contributes to each word
- If the word has only one main meaning, provide one meaning group
- The response must be valid YAML

## Examples

{EXAMPLES}

Now generate examples for: **{WORD}**