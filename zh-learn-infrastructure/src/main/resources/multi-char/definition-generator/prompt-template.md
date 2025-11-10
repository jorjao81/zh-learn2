# Chinese Multi-Character Word Definition Generation

Generate a dictionary-style definition for Chinese word: **{WORD}**
{CONTEXT}

## Task
Generate a concise, dictionary-style definition for this Chinese word. The definition should be similar to what you would find in a Chinese-English dictionary like Pleco or CC-CEDICT.

## Output Format
Return your response as plain HTML using the following patterns:

### Simple Definition
```html
<span class="part-of-speech">part of speech</span> definition text
```

### Definition with Multiple Meanings
```html
<span class="part-of-speech">verb</span>
<ol>
    <li>primary meaning</li>
    <li>secondary meaning</li>
</ol>
```

### Domain-Specific Definitions
```html
<span class="part-of-speech">noun</span> <span class="domain">domain</span> definition
```

### Multiple Parts of Speech
```html
<span class="part-of-speech">noun</span>
<ol>
    <li>first noun meaning</li>
</ol>
<span class="part-of-speech">verb</span>
<ol>
    <li>first verb meaning</li>
    <li>second verb meaning</li>
</ol>
```

## Guidelines

### Content Requirements
- Generate concise, dictionary-style definitions
- Focus on the most common meanings first
- Use clear, simple English
- Include part-of-speech tags
- Add domain markers where relevant (medicine, law, mathematics, etc.)
- For compound words, consider the meaning formed by the constituent characters

### HTML Structure Rules
- Use proper HTML5 semantics
- Ensure all tags are properly closed
- Use `<ol>` for ordered lists of meanings when there are multiple
- Use `<li>` for individual definitions
- Use `<span class="part-of-speech">` for grammatical categories
- Use `<span class="domain">` for subject domains
- Use `<span class="usage">` for usage notes (colloquial, formal, dialect, etc.)

### Class Names
- `part-of-speech`: For grammatical categories (noun, verb, adjective, adverb, etc.)
- `usage`: For usage notes (colloquial, formal, literary, etc.)
- `usage dialect`: For dialect-specific usage
- `usage colloquial`: For colloquial usage
- `usage figurative`: For figurative usage
- `domain`: For subject domains (medicine, law, mathematics, physics, etc.)

### Style Guidelines
- Keep definitions concise and clear
- Use semicolons to separate synonyms
- Use numbered lists for distinct meanings
- Avoid overly elaborate explanations
- Focus on practical, commonly-used meanings

### What NOT To Do
- WRONG: Generate verbose, encyclopedic explanations
- WRONG: Include etymology or character breakdowns (that's a separate field)
- WRONG: Add usage examples (those go in the examples field)
- WRONG: Include explanatory text or meta-commentary
- WRONG: Output anything other than clean HTML

{EXAMPLES}

Now generate a dictionary-style definition for: **{WORD}**
Remember: output only clean HTML (no extra prose, markdown formatting, or explanations).
