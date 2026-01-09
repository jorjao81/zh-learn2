# ZH Learn - Chinese Learning Tool

A modular CLI application for Chinese language learning with AI-powered analysis. Features include word analysis (pinyin, definitions, examples, etymological explanations, structural decomposition) and multi-source audio pronunciation.

## Prerequisites

- Java 25+ with preview features enabled
- Maven 3.8+
- GraalVM (optional, for native compilation)
- pre-commit (for git hooks)

## Development Setup

```bash
# Install pre-commit hooks
pip install pre-commit
pre-commit install
```

## Build

```bash
# Build all modules
mvn clean package

# Quick build (CLI and dependencies only)
mvn -pl zh-learn-cli -am package

# Build native executable (requires GraalVM)
cd zh-learn-cli && mvn native:compile-no-fork -Pnative
```

## Run

### Modular JVM execution
```bash
./zh-learn.sh parse-pleco input.tsv --export-anki=output.tsv
```

### Native executable
```bash
./zh-learn-cli/target/zh-learn parse-pleco input.tsv --export-anki=output.tsv
```

## Performance

- **Native executable**: 27ms startup time
- **Modular JVM**: 144ms startup time
- **Native size**: 30.89MB (standalone)

## Test

```bash
mvn test
```

## CLI Commands

### `providers` - List Available Providers
```bash
./zh-learn.sh providers [--type AI|DICTIONARY|LOCAL|DUMMY] [--class PINYIN|DEFINITION|...] [--detailed|-d]
```

### `parse-pleco` - Parse Pleco Export and Analyze Words
Parse Pleco export files and process all words through the analysis pipeline.

```bash
./zh-learn.sh parse-pleco <file.tsv> [options]
```

**Options:**
- `--pinyin-provider` - Provider for pinyin (default: pleco-export). Available: pinyin4j, dummy, pleco-export
- `--definition-provider` - Provider for definitions (default: pleco-export). Available: dummy, pleco-export
- `--definition-formatter-provider` - Format existing definitions via AI (default: deepseek-chat)
- `--definition-generator-provider` - Generate missing definitions via AI
- `--explanation-provider` - Generate etymological explanations (default: deepseek-chat)
- `--example-provider` - Generate example sentences (default: deepseek-chat)
- `--decomposition-provider` - Generate structural decomposition (default: deepseek-chat)
- `--audio-provider` - Audio provider (default: anki). Available: anki, forvo, qwen-tts, tencent-tts, minimax-tts
- `--audio-selections` - Pre-specify audio choices (format: `word:provider:description;...`)
- `--parallel-threads` - Number of parallel processing threads (default: 10)
- `--disable-parallelism` - Use sequential processing instead
- `--export-anki` - Output file path for Anki TSV export
- `--limit` - Process only N words
- `--skip-audio` - Skip interactive audio selection
- `--model` - AI model for OpenRouter (e.g., `gpt-4`, `claude-3-sonnet`)
- `--raw` - Display raw HTML content instead of formatted output

**Example:**
```bash
./zh-learn.sh parse-pleco Chinese.txt \
  --explanation-provider glm-4.5 \
  --example-provider glm-4.5 \
  --decomposition-provider glm-4.5 \
  --audio-provider qwen-tts \
  --limit 5 \
  --export-anki=output.tsv
```

### `parse-anki` - Parse and Display Anki Collection
```bash
./zh-learn.sh parse-anki <file.tsv>
```
Parses Anki export files and displays parsed notes (supports Chinese 2 note type).

### `improve-anki` - Enhance Existing Anki Exports
Selectively regenerate fields in existing Anki exports while preserving others.

```bash
./zh-learn.sh improve-anki <file.tsv> [options]
```

**Options:**
- `--improve-audio` - Regenerate audio pronunciations
- `--improve-explanation` - Regenerate etymological explanations
- `--improve-examples` - Regenerate example sentences
- `--improve-decomposition` - Regenerate structural decomposition
- `--improve-definition` - Regenerate definitions
- `--audio-provider` - Audio provider selection
- `--audio-selections` - Pre-specify audio choices
- `--explanation-provider`, `--example-provider`, `--decomposition-provider` - AI provider selection
- `--parallel-threads` - Parallel processing threads
- `--export-anki` - Output file path

### `audio` - Query Pronunciation by Pinyin
```bash
./zh-learn.sh audio <chinese-word> <pinyin> [--audio-provider <name>]
```

**Example:**
```bash
# Query from Anki collection
./zh-learn.sh audio 学习 xuéxí

# Query from Qwen TTS
./zh-learn.sh audio 学习 xuéxí --audio-provider qwen-tts
```

### `audio-select` - Interactive Audio Selection
```bash
./zh-learn.sh audio-select <chinese-word> <pinyin>
```
Interactive terminal UI for selecting pronunciation from multiple providers:
- Arrow keys: Navigate options
- Space: Replay current audio
- Enter: Select audio
- Esc: Skip selection

## AI Providers

All AI providers support: examples, explanations, structural decomposition, definition formatting, and definition generation.

Set API keys via environment variables or JVM `-D` properties.

### DeepSeek
- **Environment:** `DEEPSEEK_API_KEY`
- **Base URL:** `DEEPSEEK_BASE_URL` (default: `https://api.deepseek.com/v1`)
- **Provider name:** `deepseek-chat`

### ChatGLM via Zhipu
- **Environment:** `ZHIPU_API_KEY`
- **Base URL:** `ZHIPU_BASE_URL`
- **Provider names:** `glm-4-flash`, `glm-4.5`

### Alibaba Qwen (DashScope)
- **Environment:** `DASHSCOPE_API_KEY`
- **Base URL:** `DASHSCOPE_BASE_URL` (default: `https://dashscope.aliyuncs.com/compatible-mode/v1`)
- **Provider names:** `qwen-max`, `qwen-plus`, `qwen-turbo`

### Google Gemini
- **Environment:** `GEMINI_API_KEY`
- **Provider names:** `gemini-2.5-flash`, `gemini-2.5-pro`, `gemini-3-pro-preview`

### OpenRouter (Multi-model Proxy)
- **Environment:** `OPENROUTER_API_KEY`
- **Base URL:** `OPENROUTER_BASE_URL`
- **Provider name:** `openrouter`
- **Usage:** Requires `--model` parameter (e.g., `gpt-4`, `claude-3-sonnet`, `llama-2-70b-chat`)

### Dummy (Testing)
- **Provider name:** `dummy`
- No API key required

**Example - Select providers at runtime:**
```bash
./zh-learn.sh providers -d
./zh-learn.sh parse-pleco input.tsv \
  --explanation-provider glm-4.5 \
  --example-provider qwen-plus \
  --decomposition-provider deepseek-chat \
  --export-anki=output.tsv
```

## Audio Providers (Pronunciation)

### Anki (`anki`)
- Reads from Anki export at `~/.zh-learn/Chinese.txt`
- Returns pronunciation matching exact pinyin (tone marks included)
- Supports note type: "Chinese 2" (columns: 0=NoteType, 1=Simplified, 2=Pinyin, 3=Pronunciation)
- No API key required

### MiniMax TTS (`minimax-tts`)
- MiniMax Speech-2.6-HD (ranked #1 globally for TTS quality)
- **Environment:** `MINIMAX_API_KEY`, `MINIMAX_GROUP_ID`
- **Voices:** Wise_Woman, Deep_Voice_Man, Young_Knight, Calm_Woman
- Excellent Mandarin Chinese support

### Qwen TTS (`qwen-tts`)
- Alibaba Qwen text-to-speech (qwen3-tts-flash model)
- **Environment:** `DASHSCOPE_API_KEY` (international Singapore endpoint)
- **Voices:** Cherry, Ethan, Nofish, Jennifer, Elias
- Automatically caches all voices offline

### Tencent Cloud TTS (`tencent-tts`)
- Tencent Cloud text-to-speech
- **Environment:** `TENCENT_SECRET_ID`, `TENCENT_API_KEY`
- **Optional:** `TENCENT_REGION` (default: `ap-singapore`)
- **Voices:** zhiwei, zhiling

### Forvo (`forvo`)
- Fetches pronunciations from Forvo dictionary (human recordings)
- **Environment:** `FORVO_API_KEY` or `-Dforvo.api.key`
- Manual selection only

**Example:**
```bash
./zh-learn.sh providers -d

# MiniMax TTS (high quality)
./zh-learn.sh audio 学习 xuéxí --audio-provider minimax-tts

# Qwen TTS (generates 5 voice options)
./zh-learn.sh audio 学习 xuéxí --audio-provider qwen-tts

# Tencent TTS
./zh-learn.sh audio 学习 xuéxí --audio-provider tencent-tts
```

## Anki Integration

### Anki Collection Export
- Default location: Export your Anki collection (TSV) as `Chinese.txt` to `~/.zh-learn/Chinese.txt`
- The file must have the first column as the Note Type
- Supported note type: `Chinese 2` with columns:
  - 0: Note Type
  - 1: Simplified
  - 2: Pinyin
  - 3: Pronunciation

### Audio Playback Configuration
- Default Anki media directory on macOS: `~/Library/Application Support/Anki2/User 1/collection.media/`
- Override with `ZHLEARN_ANKI_MEDIA_DIR` or `-Dzhlearn.anki.media.dir=/path/to/collection.media`

### Interactive Audio Selection
When using `parse-pleco` without `--skip-audio`:
- Requires an interactive terminal (JLine raw mode)
- Run from a real TTY; piping or redirecting output will exit immediately
- Each analyzed word launches the audio selector
- Selected audio is normalized and copied into the configured Anki media directory
- Exported `[sound:...]` references resolve inside Anki

## Caching

- **AI Response Cache:** `~/.zh-learn/cache/` - Stores LLM responses by request hash
- **Audio Cache:** `~/.zh-learn/audio/<provider>/` - Stores downloaded pronunciations by provider

## Module Structure

```text
zh-learn-parent (root)
├── zh-learn-domain       - Core business logic, interfaces (no external deps)
├── zh-learn-pinyin       - Pinyin utilities (tone conversion)
├── zh-learn-infrastructure - Provider implementations (LLM, audio, dictionary, cache)
├── zh-learn-application  - Service orchestration layer
├── zh-learn-cli          - Picocli command-line interface
└── zh-learn-e2e          - End-to-end Cucumber tests
```

## Environment Variables Summary

### AI Providers (choose at least one)

| Variable | Provider | Required |
|----------|----------|----------|
| `DEEPSEEK_API_KEY` | DeepSeek | For deepseek-chat |
| `ZHIPU_API_KEY` | ChatGLM/Zhipu | For glm-4-flash, glm-4.5 |
| `DASHSCOPE_API_KEY` | Qwen LLM & TTS | For qwen-max/plus/turbo, qwen-tts |
| `GEMINI_API_KEY` | Google Gemini | For gemini-* |
| `OPENROUTER_API_KEY` | OpenRouter | For openrouter |

### Audio Providers

| Variable | Provider | Required |
|----------|----------|----------|
| `MINIMAX_API_KEY` | MiniMax TTS | For minimax-tts |
| `MINIMAX_GROUP_ID` | MiniMax TTS | For minimax-tts |
| `FORVO_API_KEY` | Forvo | For forvo |
| `TENCENT_SECRET_ID` | Tencent TTS | For tencent-tts |
| `TENCENT_API_KEY` | Tencent TTS | For tencent-tts |

### Optional Base URLs

| Variable | Default |
|----------|---------|
| `DEEPSEEK_BASE_URL` | `https://api.deepseek.com/v1` |
| `DASHSCOPE_BASE_URL` | `https://dashscope.aliyuncs.com/compatible-mode/v1` |
| `OPENROUTER_BASE_URL` | OpenRouter default |
| `TENCENT_REGION` | `ap-singapore` |

### Anki Integration

| Variable | Description |
|----------|-------------|
| `ZHLEARN_ANKI_MEDIA_DIR` | Override Anki media directory |

## License

See `LICENSE` for licensing information.
