# Vocabulary Generation Prompts

AI prompts for generating vocabulary markdown files from Pleco data.

## System Prompt (shared)

```text
You are a Chinese linguistics expert creating educational vocabulary files
for intermediate to advanced learners. Your explanations combine etymological
depth with practical usage guidance.

Key Requirements:
1. Mark the target word with == in ALL example sentences
   (hanzi, pinyin, and English)
2. Use arrow notation for semantic evolution: (orig.) X → Y → Z
3. Include natural, contemporary example sentences
4. Focus on practical usage patterns over academic theory
5. Be concise but thorough
```

## Single Character Prompt

```text
Create a vocabulary entry for the single Chinese character below.

**Character**: {hanzi}
**Pinyin**: {pinyin}
**Definition**: {definition}

Generate following FORMAT-SINGLE-CHAR.md structure:

# {character}

## Definition

{Parse definition by part of speech: "1. **pos** definition"}

## 1. Semantic Evolution

{Use arrow notation for meaning development:}
(orig.) {original meaning} → {intermediate} → {current meaning}

{Explain the semantic development with historical context.}

## 2. Components

| Part      | Type     | Function                    |
|-----------|----------|-----------------------------|
| {radical} | Radical  | Semantic: {meaning}         |
| {phonetic}| Phonetic | Sound: {pronunciation hint} |

{Explain how components combine. For ideographic compounds,
describe how parts create meaning together.}

## 3. Usage Patterns

### A. {Usage Type}

{Brief explanation.}

- {Sentence with ==character== marked}
  - _{Pinyin with ==char== marked}_
  - {English with ==translation== marked}

{2-3 examples per usage pattern.}

## 4. Compounds

| Compound | Pinyin   | Meaning   | Role of {char}     |
|----------|----------|-----------|-------------------|
{List 5-7 common compounds containing this character.}

## 5. Related Characters

| Char | Pinyin | Meaning   | Relationship           |
|------|--------|-----------|------------------------|
{List characters with same radical, phonetic, or easily confused.}

## 6. Common Errors

{1-2 common mistakes learners make.}

❌ {Incorrect}
✅ {Correct}

## 7. Notes

- **HSK Level**: {estimate}
- **Frequency**: {very common / common / uncommon / rare}
- **Register**: {formal / informal / neutral / literary}
- **Radical**: {radical} ({name})
- **Strokes**: {count}

---

CRITICAL: Mark target character with == in ALL examples.
```

## Multi-Character Prompt

```text
Create a vocabulary entry for the Chinese word below.

**Word**: {hanzi}
**Pinyin**: {pinyin}
**Definition**: {definition}

Determine word type:
- 2-3 characters without fixed structure = compound
- 4 characters with fixed structure = idiom (成语)
- 5+ characters or full sentence = proverb (谚语)

Generate following FORMAT-MULTI-CHAR.md structure:

# {word}

## Definition

{For compounds:}
1. **{pos}** {definition}

{For idioms:}
**idiom** {figurative meaning}

- **Literal**: {word-for-word translation}
- **Figurative**: {actual meaning}

{For proverbs:}
**proverb** {translation}

- **Meaning**: {explanation}

## 1. Core Meaning

{For compounds:}
**{word}** combines: **{char1} ({meaning}) + {char2} ({meaning})**

{Explain how characters create compound meaning.}

{For idioms:}
**Literal**: {word-for-word}
**Figurative**: {actual meaning}

{Origin story or cultural context.}

{For proverbs:}
**Clause breakdown**:
- {clause1}: {meaning}
- {clause2}: {meaning}

{Philosophical significance.}

## 2. Character Breakdown

| Character | Pinyin | Meaning    | Role in Word          |
|-----------|--------|------------|-----------------------|
{For each character in the word.}

{For proverbs, use clause breakdown instead.}

## 3. Usage Patterns

### A. {Primary Usage}

{Brief explanation.}

- {Sentence with ==word== marked}
  - _{Pinyin with ==word== marked}_
  - {English with ==translation== marked}

{2-3 examples per pattern.}

## 4. Structural Analysis

{Word structure type, parts of speech, syntactic positions,
usage constraints.}

## 5. Related Words

| Word    | Pinyin | Meaning   | Relationship      |
|---------|--------|-----------|-------------------|
{4-6 synonyms, antonyms, related concepts.}

## 6. Common Collocations

- {collocation} ({pinyin}) - {meaning}
{4-6 common word pairings.}

## 7. Comparison

{Compare with similar/confused words if applicable.}

| Feature  | {word1}          | {word2}          |
|----------|------------------|------------------|
| Register | {description}    | {description}    |
| Nuance   | {description}    | {description}    |

## 8. Common Errors

{1-2 common mistakes.}

❌ {Incorrect}
✅ {Correct}

## 9. Notes

- **Type**: {compound / idiom / proverb}
- **HSK Level**: {estimate}
- **Frequency**: {very common / common / uncommon / rare}
- **Register**: {formal / informal / neutral / literary / colloquial}

---

CRITICAL: Mark target word with == in ALL examples.
```

## Usage in Code

```java
// Determine which prompt to use
String prompt = hanzi.length() == 1
    ? singleCharPrompt(entry)
    : multiCharPrompt(entry);

String singleCharPrompt(PlecoEntry entry) {
    return String.format("""
        Create a vocabulary entry for the single Chinese character below.

        **Character**: %s
        **Pinyin**: %s
        **Definition**: %s
        ...
        """, entry.hanzi(), entry.pinyin(), entry.definitionText());
}

String multiCharPrompt(PlecoEntry entry) {
    return String.format("""
        Create a vocabulary entry for the Chinese word below.

        **Word**: %s
        **Pinyin**: %s
        **Definition**: %s
        ...
        """, entry.hanzi(), entry.pinyin(), entry.definitionText());
}
```

## Quality Checklist

### Single Character

- [ ] Title is character only (no pinyin)
- [ ] Semantic evolution uses arrow notation
- [ ] Components table shows radical and phonetic
- [ ] Examples mark character with ==
- [ ] Compounds table has 5+ entries
- [ ] Notes include radical and stroke count

### Multi-Character

- [ ] Title is word only (no pinyin)
- [ ] Word type identified (compound/idiom/proverb)
- [ ] Idioms have literal + figurative meaning
- [ ] Character breakdown table complete
- [ ] Examples mark word with ==
- [ ] Related words table has 4+ entries

## Examples

- Single character: `single/晶.md`
- Compound word: `multi/学习.md`
- Idiom: `multi/生龙活虎.md`
