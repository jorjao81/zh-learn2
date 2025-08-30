# Chinese Word Examples Generator

Generate usage examples for the Chinese word: **{WORD}**
{CONTEXT}

Please provide examples grouped by different meanings or usages of this word.

## Output Format
Return your response as YAML in the following structure:

```yaml
response:
  - meaning: "description of meaning 1"
    examples:
      - hanzi: "Chinese word containing the character"
        pinyin: "phonetic transcription"
        translation: "English translation"
      - hanzi: "Another Chinese word containing the character"
        pinyin: "phonetic transcription" 
        translation: "English translation"
  - meaning: "description of meaning 2 (if applicable)"
    examples:
      - hanzi: "Chinese word for meaning 2"
        pinyin: "phonetic transcription"
        translation: "English translation"
```

## Requirements
- Group examples by distinct meanings/usages of the word
- Provide 1-4 examples per meaning
- Use natural, commonly used sentences
- Include accurate pinyin with tone marks
- Provide clear English translations
- If the word has only one main meaning, provide one meaning group
- The response must be valid YAML

## Examples

{EXAMPLES}

Now generate examples for: **{WORD}**