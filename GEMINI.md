# GEMINI.md — Chinese Learning Project

This project contains linguistic resources for learning Chinese, including HSK grammar references, exercises, and Anki card generation.

## Project Structure

```text
linguistics/
├── hsk/
│   ├── grammar/
│   │   ├── HSK3.0-Level3-Grammar.md   # Official HSK 3.0 grammar points
│   │   ├── HSK3.0-Level4-Grammar.md
│   │   ├── HSK3.0-Level5-Grammar.md
│   │   ├── HSK4-14-对于.md            # Detailed grammar explanations
│   │   └── characters/                 # Character breakdowns
│   │       ├── 对于.md
│   │       ├── 关于.md
│   │       └── 替.md
│   └── HSK4-Official-Syllabus.pdf
├── exercises/
│   ├── Exercise-HighlightedTranslationCard.md
│   ├── grammar-sentence-card.css
│   └── word-memo.css
├── standards/
│   └── Chinese-Proficiency-Standards-Official.pdf
└── llm/
    └── system-prompt.md               # System prompt for AI assistants
```

## Key Guidelines

When generating content for this project:

1. **Language**: Explain everything in English
2. **Linguistic Accuracy**: Use proper linguistic terminology and avoid simplifications
3. **Cultural Authenticity**: Avoid Western-created mnemonics; explain Chinese as Chinese speakers understand it
4. **Pinyin Format**: Use tone marks (jīhū) not numbers (ji1hu1)

## HSK Grammar Point Format

Grammar points in HSK3.0-Level*.md files follow this format:

| # | Category | Chinese | Pinyin | Examples |
|---|----------|---------|--------|----------|
| 四14 | Regarding/As for | 对于 | duìyú | Example sentences... |

The ID format is: `{level}{number}` where level is 三/四/五 (3/4/5) and number is 01-99.

## Custom Commands

- `/hsk:generate:concept <word>` — Generate detailed grammar explanation and character breakdown
