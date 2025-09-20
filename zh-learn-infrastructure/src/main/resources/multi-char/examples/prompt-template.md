# Chinese Sentence Examples from Multi-Character Words

Generate sentence examples for the Chinese multi-character word: **{WORD}**
{CONTEXT}

Please provide sentence examples grouped by different meanings or parts-of-speech of this word.

## Output Format
Return your response as YAML in the following structure:

```yaml
words:
  - meaning: "description of meaning/part-of-speech 1"
    pinyin: "pronunciation for this word"
    examples:
      - hanzi: "Short Chinese sentence with <b>word</b> highlighted"
        pinyin: "phonetic transcription with <b>pinyin</b> highlighted"
        translation: "English translation with <b>word</b> highlighted"
        breakdown: "Usage context or grammatical note"
      - hanzi: "Another Chinese sentence with <b>word</b> highlighted"
        pinyin: "phonetic transcription with <b>pinyin</b> highlighted"
        translation: "English translation with <b>word</b> highlighted"
        breakdown: "Usage context or grammatical note"
  - meaning: "description of meaning/part-of-speech 2 (if applicable)"
    pinyin: "pronunciation for this word"
    examples:
      - hanzi: "Chinese sentence for meaning 2 with <b>word</b> highlighted"
        pinyin: "phonetic transcription with <b>pinyin</b> highlighted"
        translation: "English translation with <b>word</b> highlighted"
        breakdown: "Usage context or grammatical note"

phonetic_series: []
```

## Requirements
- Group examples by distinct meanings or parts-of-speech of the multi-character word
- Include the pinyin pronunciation for each meaning group
- Provide 2-4 sentence examples per meaning/part-of-speech
- Create short, realistic sentences that demonstrate actual usage
- Highlight the target word with `<b>` tags in hanzi, pinyin, and translation
- Include accurate pinyin with tone marks for the entire sentence
- Provide clear English translations of the full sentence
- Include breakdown explanations about usage context or grammatical function
- If the word has only one main meaning, provide one meaning group
- Always set `phonetic_series: []` (empty) for multi-character words
- The response must be valid YAML

### STRICT OUTPUT RULES
- Output MUST be only YAML (prefer a single YAML code block or raw YAML). Do NOT include any prose before or after.
- Use spaces (2 spaces) for indentation. Do NOT use tabs.
- Keys must be exactly: words, meaning, pinyin, examples, hanzi, translation, breakdown, phonetic_series.
- Always include top-level `phonetic_series: []` (empty list).
- Quote all string values with double quotes. If a value contains double quotes, escape them (\").
- Keep values on a single line; avoid multi-line scalars. Use commas/semicolons if needed.
- Do NOT include Markdown, backticks, or HTML in the output except for `<b>` tags to highlight the target word.
- Avoid colons in unquoted values; ensure any text with colons is quoted.
- Validate that the YAML is syntactically correct and parsable before responding.

## Examples

{EXAMPLES}

Now generate sentence examples for: **{WORD}**
Remember: output only the YAML (no extra prose).