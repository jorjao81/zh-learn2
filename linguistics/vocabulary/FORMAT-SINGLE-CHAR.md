# Single Character Vocabulary Format

Format for single Chinese characters (单字).

## File Naming

- Pattern: `{character}.md`
- Example: `晶.md`, `键.md`, `帽.md`
- Location: `linguistics/vocabulary/single/`

## Sections Overview

| #   | Section            | Req | Source |
|-----|--------------------|-----|--------|
| -   | Title              | Yes | Pleco  |
| -   | Definition         | Yes | Pleco  |
| 1   | Semantic Evolution | Yes | AI     |
| 2   | Components         | Yes | AI     |
| 3   | Usage Patterns     | Yes | AI     |
| 4   | Compounds          | Yes | AI     |
| 5   | Related Characters | No  | AI     |
| 6   | Common Errors      | No  | AI     |
| 7   | Notes              | Yes | Mixed  |

## Semantic Evolution Format

Use arrow notation for meaning development:

```text
(orig.) X → Y → Z
```

- `→` for gradual semantic shift
- `⇒` for larger conceptual leaps

Examples:

- `(orig.) stars → brilliant → crystal`
- `(orig.) door bolt → linchpin → key (keyboard)`
- `(orig.) placenta → womb → born of same parents ⇒ compatriot`

## Template

```markdown
# {character}

## Definition

1. **{pos}** {definition}
2. **{pos}** {definition}

## 1. Semantic Evolution

(orig.) {original meaning} → {intermediate} → {current meaning}

{Explanation of the semantic development, including historical context
if relevant.}

## 2. Components

| Part     | Type     | Function                    |
|----------|----------|-----------------------------|
| {radical}| Radical  | Semantic: {meaning}         |
| {phonetic}| Phonetic| Sound: {pronunciation hint} |

{Additional notes on character structure. For ideographic compounds,
explain how the components combine to create meaning.}

## 3. Usage Patterns

### A. {Usage Type}

{Brief explanation.}

- {Sentence with ==character== marked}
  - _{Pinyin with ==char== marked}_
  - {English with ==translation== marked}

- {Second example}
  - _{Pinyin}_
  - {English}

### B. {Another Usage} (if applicable)

{Brief explanation and examples.}

## 4. Compounds

Common words containing this character:

| Compound | Pinyin   | Meaning        | Role of {char}     |
|----------|----------|----------------|--------------------|
| {word1}  | {pinyin} | {meaning}      | {role in compound} |
| {word2}  | {pinyin} | {meaning}      | {role in compound} |

## 5. Related Characters

| Char | Pinyin | Meaning    | Relationship           |
|------|--------|------------|------------------------|
| {c1} | {py}   | {meaning}  | {same radical/phonetic}|
| {c2} | {py}   | {meaning}  | {easily confused}      |

## 6. Common Errors

### {Error Type}

{Description of common mistake.}

❌ {Incorrect}
✅ {Correct}

## 7. Notes

- **HSK Level**: {HSK 1-6 or "Above HSK 6"}
- **Frequency**: {very common / common / uncommon / rare}
- **Register**: {formal / informal / neutral / literary}
- **Radical**: {radical} ({radical name})
- **Strokes**: {stroke count}

---

source: pleco
created: {YYYY-MM-DD}
status: draft
```
