# Chinese Character Definition Formatting

Format the definition for Chinese character: **{WORD}**
{CONTEXT}

## Task

ONLY format the provided raw definition - DO NOT generate new content under any circumstances. Focus on clean HTML presentation and expanding abbreviations to full words.

## Input Definition

{RAW_DEFINITION}

## Output Format

Return your response as a clean HTML definition using the following structure:

```html
<span class="part-of-speech">part of speech</span> formatted definition text with appropriate HTML structure
```

## Guidelines

### Single Character Processing

- For single characters, keep definitions concise and focused
- Add part-of-speech tagging using `<span class="part-of-speech">` tags
- ALWAYS expand abbreviations to their full word forms - NEVER keep abbreviated forms
- Use semantic HTML where appropriate:
  - `<em>` for emphasis
  - `<strong>` for key terms
  - Use semicolons to separate different meanings
  - Use commas to separate related concepts

### HTML Structure Rules

- Always include part-of-speech tagging as the first element
- Use proper HTML semantics
- Keep the structure clean and readable
- Avoid complex nested structures for single characters
- Ensure proper closing of all HTML tags

### Abbreviation Expansion - MANDATORY

ALWAYS expand the following abbreviations to their full word forms:

- adj. → adjective
- adv. → adverb
- conj. → conjunction
- interj. → interjection
- n. → noun
- prep. → preposition
- v. → verb
- etc. → and so forth
- i.e. → that is
- e.g. → for example

### Content Preservation - CRITICAL

- IMPORTANT: Preserve the original meaning exactly. Only add HTML formatting and expand abbreviations.
- NEVER add new definitions, meanings, or explanations not present in the original.
- If the original definition is brief or simple, keep it brief - only format what's provided.
- DO NOT elaborate, expand, or add examples unless they exist in the original.

### Example Outputs

For input: "v. to estimate; to assess; evaluation"
Output:

```html
<span class="part-of-speech">verb</span> to estimate; to assess; evaluation
```

For input: "n. ancient Chinese musical instrument; bell"
Output:

```html
<span class="part-of-speech">noun</span> ancient Chinese musical instrument; bell
```

For input: "adj. flat, level, even"
Output:

```html
<span class="part-of-speech">adjective</span> flat, level, even
```

### What NOT To Do

- WRONG: Generate new definitions for characters like 牙套
- WRONG: Keep abbreviations like "adj." or "n." in their abbreviated form
- WRONG: Add meanings not present in the original definition
- WRONG: Include explanatory text or comments in your output

{EXAMPLES}

Now format the definition for: **{WORD}**
Remember: output only clean HTML (no extra prose or explanations).
