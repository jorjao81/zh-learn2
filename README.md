# ZH Learn - Chinese Learning Tool

Experimenting how to ensure that AI agentic coding results into reasonably maintainable
code.

## Prerequisites

- Java 24+ with GraalVM (for native compilation)
- Maven 3.8+

## Build

```bash
# Build all modules
mvn clean package

# Build native executable (requires GraalVM)
cd zh-learn-cli && mvn native:compile-no-fork -Pnative
```

## Run

### Modular JVM execution
```bash
./zh-learn.sh word 学习
```

### Audio lookup (Existing Anki Pronunciation)
- Ensure a TSV export of your Anki collection is present at `Chinese.txt` in the project root. The file must have the first column as the Note Type and only rows with Note Type `Chinese` or `Chinese 2` are considered. The observed column order is:
  - 0: Note Type (e.g., `Chinese`)
  - 1: Pinyin (with tone marks, e.g., `xuéxí`)
  - 2: Simplified (e.g., `学习`)
  - 3: Pronunciation field (e.g., `[sound:xuéxí.mp3]`)

```bash
# Query an existing pronunciation by exact pinyin match
./zh-learn.sh audio 学习 xuéxí
# Output (if present in your collection):
[sound:xuéxí.mp3]

# Specify provider explicitly (optional)
./zh-learn.sh audio 学 xué --audio-provider existing-anki-pronunciation
```

### Native executable
```bash
./zh-learn-cli/target/zh-learn word 学习
```


## Performance

- **Native executable**: 27ms startup time
- **Modular JVM**: 144ms startup time
- **Native size**: 30.89MB (standalone)

## Test

```bash
mvn test
```

## Providers: Audio (Pronunciation)

- Audio providers return an Anki-ready string (e.g., `[sound:...mp3]`) for a given word and pinyin.
- Current provider:
  - `existing-anki-pronunciation`: scans `Chinese.txt` and reuses the pronunciation of any card with the exact same pinyin (tone marks included). If nothing is found, it returns no pronunciation.

List providers and capabilities:
```bash
./zh-learn.sh providers -d
```
