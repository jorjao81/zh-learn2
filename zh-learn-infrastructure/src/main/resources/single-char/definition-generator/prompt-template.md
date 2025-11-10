# Chinese Single-Character Definition Generation

Generate a dictionary-style definition for Chinese character: **{WORD}**
{CONTEXT}

## Task
Generate a concise, dictionary-style definition for this single Chinese character. The definition should be similar to what you would find in a Chinese-English dictionary like Pleco or CC-CEDICT.

## Output Format
Return your response as plain HTML using the following patterns:

### Simple Definition
```html
<span class="part-of-speech">part of speech</span> definition text
```

### Definition with Multiple Meanings
```html
<span class="part-of-speech">noun</span>
<ol>
    <li>primary meaning</li>
    <li>secondary meaning</li>
</ol>
```

### Multiple Parts of Speech
```html
<span class="part-of-speech">noun</span> primary meaning
<span class="part-of-speech">adjective</span> adjectival meaning
```

## Guidelines

### Content Requirements
- Generate concise, dictionary-style definitions
- Focus on the character's standalone meanings (not just compound word meanings)
- Include both common modern meanings and relevant classical meanings
- Use clear, simple English
- Include part-of-speech tags where applicable
- For characters used primarily as components, note their component meaning

### HTML Structure Rules
- Use proper HTML5 semantics
- Ensure all tags are properly closed
- Use `<ol>` for ordered lists of meanings when there are multiple distinct meanings
- Use `<li>` for individual definitions
- Use `<span class="part-of-speech">` for grammatical categories
- Use `<span class="usage">` for usage notes (classical, literary, archaic, etc.)

### Class Names
- `part-of-speech`: For grammatical categories (noun, verb, adjective, etc.)
- `usage`: For usage notes (classical, literary, archaic, etc.)
- `usage classical`: For classical Chinese usage
- `usage literary`: For literary usage

### Style Guidelines
- Keep definitions concise and clear
- Use semicolons to separate synonyms
- Use numbered lists for distinct meanings
- Note if the character is primarily used in compounds
- Focus on practical, commonly-understood meanings

### What NOT To Do
- WRONG: Generate verbose explanations
- WRONG: Include etymology or character structure analysis (that's a separate field)
- WRONG: Add usage examples (those go in the examples field)
- WRONG: Include explanatory text or meta-commentary
- WRONG: Output anything other than clean HTML

{EXAMPLES}

Now generate a dictionary-style definition for: **{WORD}**
Remember: output only clean HTML (no extra prose, markdown formatting, or explanations).
