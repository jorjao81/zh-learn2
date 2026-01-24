# Multi-Character Vocabulary Format

Format for compound words (词语), idioms (成语), and proverbs (谚语).

## File Naming

- Pattern: `{word}.md`
- Example: `学习.md`, `生龙活虎.md`, `黄金易得，自由无价.md`
- Location: `linguistics/vocabulary/multi/`

## Word Types

| Type     | Chars | Example                 | Notes                  |
|----------|-------|-------------------------|------------------------|
| Compound | 2-3   | 学习, 增肥              | Verb-verb, verb-object |
| Idiom    | 4     | 生龙活虎                | Fixed structure (成语) |
| Proverb  | 5+    | 黄金易得，自由无价      | Full sentences (谚语)  |

## Sections Overview

| #   | Section            | Req | Source |
|-----|--------------------|-----|--------|
| -   | Title              | Yes | Pleco  |
| -   | Definition         | Yes | Pleco  |
| 1   | Core Meaning       | Yes | AI     |
| 2   | Character Breakdown| Yes | AI     |
| 3   | Usage Patterns     | Yes | AI     |
| 4   | Structural Analysis| No  | AI     |
| 5   | Related Words      | No  | AI     |
| 6   | Common Collocations| No  | AI     |
| 7   | Comparison         | No  | AI     |
| 8   | Common Errors      | No  | AI     |
| 9   | Notes              | Yes | Mixed  |

## Definition Format by Type

### Compound Words

```markdown
## Definition

1. **verb** to study; to learn
2. **noun** study; learning
```

### Idioms (成语)

```markdown
## Definition

**idiom** full of vim and vigor

- **Literal**: a living dragon, an active tiger
- **Figurative**: bursting with energy and vitality
```

### Proverbs (谚语)

```markdown
## Definition

**proverb** Gold is easily obtained, but freedom is priceless

- **Meaning**: Freedom is more valuable than material wealth
```

## Template

```markdown
# {word}

## Definition

{For compounds:}
1. **{pos}** {definition}

{For idioms:}
**idiom** {figurative meaning}

- **Literal**: {word-for-word translation}
- **Figurative**: {actual meaning in use}

{For proverbs:}
**proverb** {translation}

- **Meaning**: {explanation of the message}

## 1. Core Meaning

{For compounds:}
**{word}** combines: **{char1} ({meaning}) + {char2} ({meaning})**

{Explanation of how the characters create the compound meaning.}

{For idioms:}
**Literal**: {word-for-word meaning}
**Figurative**: {actual meaning}

{Origin story or cultural context if known.}

{For proverbs:}
**Clause breakdown**:
- {clause1}: {meaning}
- {clause2}: {meaning}

{Philosophical or cultural significance.}

## 2. Character Breakdown

| Character | Pinyin | Meaning    | Role in Word           |
|-----------|--------|------------|------------------------|
| {char1}   | {py1}  | {meaning1} | {function in compound} |
| {char2}   | {py2}  | {meaning2} | {function in compound} |

{For proverbs, break down by clause instead:}

| Clause    | Pinyin | Meaning   |
|-----------|--------|-----------|
| {clause1} | {py1}  | {meaning} |
| {clause2} | {py2}  | {meaning} |

## 3. Usage Patterns

### A. {Primary Usage}

{Brief explanation of this pattern.}

- {Sentence with ==word== marked}
  - _{Pinyin with ==word== marked}_
  - {English with ==translation== marked}

- {Second example}
  - _{Pinyin}_
  - {English}

### B. {Secondary Usage} (if applicable)

{Brief explanation and examples.}

## 4. Structural Analysis

{Grammatical analysis:}
- Word structure type (verb-verb, verb-object, adj-noun, etc.)
- Parts of speech it can function as
- Syntactic positions (predicate, attributive, complement)
- Any constraints on usage

## 5. Related Words

| Word    | Pinyin | Meaning    | Relationship      |
|---------|--------|------------|-------------------|
| {word1} | {py1}  | {meaning1} | {synonym/antonym} |
| {word2} | {py2}  | {meaning2} | {related concept} |

## 6. Common Collocations

- {collocation1} ({pinyin}) - {meaning}
- {collocation2} ({pinyin}) - {meaning}
- {collocation3} ({pinyin}) - {meaning}

## 7. Comparison

{When there are similar or easily confused words:}

| Feature  | {word1}           | {word2}           |
|----------|-------------------|-------------------|
| Register | {formal/informal} | {formal/informal} |
| Nuance   | {description}     | {description}     |
| Example  | {brief example}   | {brief example}   |

## 8. Common Errors

### {Error Type}

{Description of the mistake and why it's wrong.}

❌ {Incorrect usage}
✅ {Correct usage}

## 9. Notes

- **Type**: {compound / idiom / proverb}
- **HSK Level**: {HSK 1-6 or "Above HSK 6"}
- **Frequency**: {very common / common / uncommon / rare}
- **Register**: {formal / informal / neutral / literary / colloquial}

---

source: pleco
created: {YYYY-MM-DD}
status: draft
```
