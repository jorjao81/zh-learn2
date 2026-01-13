# Chinese Multi-Character Word Definition Formatting

Format the definition for Chinese word: **{WORD}**
{CONTEXT}

## Task

ONLY format the provided raw definition - DO NOT generate new content under any circumstances. Focus on clean HTML presentation and expanding abbreviations to full words.

## Input Definition

{RAW_DEFINITION}

## Output Format

Return your response as well-structured HTML using the following patterns:

### Simple Definition

```html
<span class="part-of-speech">part of speech</span> definition text
```

### Complex Definition with Multiple Meanings

```html
<span class="part-of-speech">verb</span> <span class="usage">colloquial</span>
<ol>
    <li>boast; brag; talk big</li>
    <li><span class="usage dialect">dialect</span> chat; talk casually</li>
</ol>
```

### Domain-Specific Definitions

```html
<span class="part-of-speech">noun</span> <span class="domain">anatomy</span> vein
```

### Multiple Parts of Speech

```html
<span class="part-of-speech">noun</span>
<ol>
    <li><span class="domain">medicine</span> miscarriage; abortion</li>
</ol>
<span class="part-of-speech">verb</span>
<ol>
    <li><span class="domain">medicine</span> (of a woman) have a miscarriage; miscarry; abort</li>
    <li><span class="usage">figurative</span> (of a plan, etc.) miscarry; fall through; abort</li>
</ol>
```

## Guidelines

### Multi-Character Processing

- Use numbered lists (`<ol>`) for distinct meanings
- Add part-of-speech tagging using `<span class="part-of-speech">` tags
- Mark usage notes with `<span class="usage">` or specific classes like `<span class="usage dialect">`
- Mark domains with `<span class="domain">` tags
- Expand common abbreviations
- Use semantic HTML structure

### HTML Structure Rules

- Use proper HTML5 semantics
- Ensure all tags are properly closed
- Use `<ol>` for ordered lists of meanings
- Use `<li>` for individual definitions
- Nest spans appropriately for usage/domain markers

### Class Names

- `part-of-speech`: For grammatical categories
- `usage`: For general usage notes
- `usage dialect`: For dialect-specific usage
- `usage colloquial`: For colloquial usage
- `usage figurative`: For figurative usage
- `domain`: For subject domains

### Domain Expansion

Expand domain abbreviations:

- math. → mathematics
- med. → medicine
- anat. → anatomy
- pharm. → pharmacy
- ling. → linguistics
- ecol. → ecology
- chem. → chemistry
- phys. → physics

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

### Parentheses Handling

Remove parentheses from usage/domain markers:

- Input: "(math.) higher dimensional"
- Output: `<span class="domain">mathematics</span> higher dimensional`

### Content Preservation - CRITICAL

- IMPORTANT: Preserve the original meaning exactly. Only add HTML formatting and expand abbreviations.
- NEVER add new definitions, meanings, or explanations not present in the original.
- If the original definition is brief or simple, keep it brief - only format what's provided.
- DO NOT elaborate, expand, or add examples unless they exist in the original.

### What NOT To Do

- WRONG: Generate new definitions for words like 牙套
- WRONG: Keep abbreviations like "adj." or "n." in their abbreviated form
- WRONG: Add meanings not present in the original definition
- WRONG: Include explanatory text or comments in your output

{EXAMPLES}

Now format the definition for: **{WORD}**
Remember: output only clean HTML (no extra prose or explanations).
