# Word Command Flow Documentation

## Overview

The `word` command performs comprehensive analysis of a single Chinese word using the zh-learn analysis pipeline. It provides AI-powered insights including examples, explanations, structural decomposition, and pronunciation analysis through configurable providers.

## Command Structure

```bash
./zh-learn.sh word <chinese-word> [options]
```

### Key Options
- `--pinyin-provider`: Pinyin generation (default: pinyin4j)
- `--definition-provider`: Definition lookup (default: dummy)
- `--definition-formatter-provider`: Definition formatting via AI (default: dummy)
- `--decomposition-provider`: Structural decomposition analysis (default: dummy)
- `--example-provider`: Usage examples generation (default: dummy)
- `--explanation-provider`: Linguistic explanations (default: dummy)
- `--audio-provider`: Pronunciation audio (default: anki)
- `--model`: Specific AI model for providers (e.g., gpt-4, claude-3-sonnet)
- `--raw`: Display raw HTML content instead of formatted output

## Processing Flow

### High-Level Command Flow

```mermaid
flowchart TD
    A[Start word command] --> B[Parse CLI arguments]
    B --> C[Resolve Provider Instances]
    C --> D[Create WordAnalysisService]
    D --> E[Create ProviderConfiguration]
    E --> F[Analyze Single Word]
    F --> G{Raw Output Mode?}
    G -->|Yes| H[Print Raw HTML]
    G -->|No| I[Print Formatted Output]
    H --> J[Complete]
    I --> J
```

## Provider Resolution Phase

```mermaid
flowchart TD
    A[CLI Options] --> B[MainCommand Factory Methods]
    B --> C{Provider Type?}
    C -->|AI Provider| D[AIProviderFactory]
    C -->|Audio Provider| E[Audio Provider List]
    C -->|Dictionary Provider| F[Simple Switch Statement]

    D --> G[Check API Keys]
    G --> H[Create HTTP Client]
    H --> I[Configure LangChain4j]
    I --> J[Provider Instance]

    E --> K[Find by Name]
    K --> L[Audio Provider Instance]

    F --> M[Static Provider Instance]

    J --> N[WordAnalysisServiceImpl]
    L --> N
    M --> N
```

**Provider Categories:**
- **AI Providers**: Require API keys, use LangChain4j (examples, explanations, decomposition, definition formatting)
- **Audio Providers**: File-based or API-based pronunciation (anki, forvo, qwen-tts, tencent)
- **Dictionary Providers**: Local lookups (pinyin4j, dummy)

## Single Word Analysis Pipeline

### WordAnalysisService Flow

```mermaid
sequenceDiagram
    participant Cmd as WordCommand
    participant WAS as WordAnalysisService
    participant PP as PinyinProvider
    participant DP as DefinitionProvider
    participant DFP as DefinitionFormatterProvider
    participant EP as ExampleProvider
    participant ExP as ExplanationProvider
    participant SDP as StructuralDecompositionProvider
    participant AP as AudioProvider

    Cmd->>WAS: getCompleteAnalysis(hanzi, config)

    Note over WAS: Synchronous foundational calls
    WAS->>PP: getPinyin(hanzi)
    PP-->>WAS: Pinyin
    WAS->>DP: getDefinition(hanzi)
    DP-->>WAS: Raw Definition
    WAS->>DFP: formatDefinition(hanzi, rawDefinition)
    DFP-->>WAS: Formatted Definition

    Note over WAS: Parallel AI provider calls
    par Examples
        WAS->>EP: getExamples(hanzi, definition)
        EP-->>WAS: Examples
    and Explanation
        WAS->>ExP: getExplanation(hanzi, definition)
        ExP-->>WAS: Explanation
    and Decomposition
        WAS->>SDP: getStructuralDecomposition(hanzi)
        SDP-->>WAS: Structural Decomposition
    end

    Note over WAS: Audio lookup (optional)
    WAS->>AP: getPronunciation(hanzi, pinyin)
    AP-->>WAS: Optional Audio Path

    WAS-->>Cmd: Complete WordAnalysis
```

**Processing Order:**
1. **Synchronous Phase**: Pinyin and definition (needed by other providers)
2. **Parallel Phase**: AI providers that can run concurrently
3. **Audio Phase**: Pronunciation lookup using generated pinyin

## Provider System Detail

### AI Provider Creation Flow

```mermaid
flowchart TD
    A[Provider Name + Model] --> B{Provider Type Check}
    B -->|OpenAI-compatible| C[Create OpenAI Client]
    B -->|Custom API| D[Create Custom Client]
    B -->|Dummy| E[Create Dummy Provider]

    C --> F[Configure API Key]
    F --> G[Set Base URL]
    G --> H[Create LangChain4j ChatModel]
    H --> I[Wrap in ConfigurableProvider]

    D --> J[Custom HTTP Configuration]
    J --> K[Provider-specific Client]
    K --> I

    E --> L[Static Response Provider]
    L --> I

    I --> M[Ready for WordAnalysisService]
```

**Supported AI Providers:**
- **OpenAI**: gpt-4, gpt-3.5-turbo
- **DeepSeek**: deepseek-chat, deepseek-coder
- **Qwen**: qwen-max, qwen-plus, qwen-turbo
- **Zhipu**: glm-4-flash, glm-4.5
- **OpenRouter**: Multiple models (claude, llama, etc.)
- **Gemini**: gemini-pro, gemini-1.5-pro

### Audio Provider Architecture

```mermaid
flowchart TD
    A[Audio Provider Request] --> B{Provider Type}
    B -->|anki| C[AnkiPronunciationProvider]
    B -->|forvo| D[ForvoAudioProvider]
    B -->|qwen-tts| E[QwenAudioProvider]
    B -->|tencent| F[TencentAudioProvider]

    C --> G[Local Anki Media Scan]
    D --> H[Forvo API Call]
    E --> I[Qwen TTS Synthesis]
    F --> J[Tencent TTS Synthesis]

    G --> K[File Path or None]
    H --> L[Download + Normalize]
    I --> M[Download + Normalize + Cache]
    J --> M

    L --> N[Cached Audio File]
    M --> N
    K --> O[Optional Audio Path]
    N --> O
```

**Audio Provider Characteristics:**
- **Anki**: Fastest (local file scan), no API required
- **Forvo**: Community pronunciation database, requires API key
- **Qwen TTS**: AI-generated Chinese speech, requires DASHSCOPE_API_KEY
- **Tencent**: Professional TTS service, requires Tencent Cloud credentials

## Data Processing Flow

### Core Data Transformations

```mermaid
flowchart LR
    A[CLI String Input] --> B[Hanzi Object]
    B --> C[Provider Analysis]
    C --> D[WordAnalysis Record]
    D --> E{Output Format}
    E -->|Raw| F[HTML Content]
    E -->|Formatted| G[Terminal Formatted Text]
    F --> H[Console Output]
    G --> H
```

### WordAnalysis Components

```mermaid
classDiagram
    class WordAnalysis {
        +Hanzi word
        +Pinyin pinyin
        +Definition definition
        +StructuralDecomposition decomposition
        +Example examples
        +Explanation explanation
        +Optional~Path~ audioFile
        +getFormattedOutput() String
        +getRawHtml() String
    }

    class ProviderConfiguration {
        +String exampleProvider
        +String pinyinProvider
        +String definitionProvider
        +String definitionFormatterProvider
        +String decompositionProvider
        +String explanationProvider
        +String audioProvider
    }

    class Hanzi {
        +String characters
        +getComplexity() int
        +isSingleCharacter() boolean
    }

    class Pinyin {
        +String pinyin
        +getToneNumbers() String
        +getToneMarks() String
    }

    WordAnalysis --> Hanzi
    WordAnalysis --> Pinyin
    WordAnalysis --> ProviderConfiguration
```

## Output Formatting

### Terminal Output Pipeline

```mermaid
flowchart TD
    A[WordAnalysis] --> B{Raw Mode?}
    B -->|Yes| C[Extract HTML Content]
    B -->|No| D[AnalysisPrinter.printFormatted]

    C --> E[Print Raw HTML Strings]

    D --> F[Format Hanzi + Pinyin Header]
    F --> G[Format Definition Section]
    G --> H[Format Examples with ANSI Colors]
    H --> I[Format Explanation]
    I --> J[Format Structural Decomposition]
    J --> K[Format Audio Info]
    K --> L[Terminal Output]

    E --> M[Raw Console Output]
```

**Formatting Features:**
- **ANSI Colors**: Syntax highlighting for different content types
- **Unicode Support**: Proper rendering of Chinese characters and tone marks
- **Responsive Layout**: Adapts to terminal width
- **HTML Fallback**: Raw mode preserves original formatting

## Error Handling Strategy

```mermaid
flowchart TD
    A[Word Command Execution] --> B{Provider Creation Succeeds?}
    B -->|No| C[Fail Fast with Clear Error]
    B -->|Yes| D[Begin Analysis]

    D --> E{Core Providers Available?}
    E -->|No| F[Log Warning + Continue with Partial Analysis]
    E -->|Yes| G[Full Analysis Pipeline]

    G --> H{AI Provider Calls Succeed?}
    H -->|Partial| I[Continue with Available Results]
    H -->|Complete| J[Full Analysis Complete]

    C --> K[Exit with Error Code]
    F --> L[Display Partial Results]
    I --> L
    J --> M[Display Complete Results]
```

**Error Philosophy:**
- **Fail Fast**: Invalid configurations terminate immediately
- **Graceful Degradation**: Missing AI providers result in partial analysis
- **User Feedback**: Clear error messages with actionable guidance
- **No Silent Failures**: All errors logged or displayed to user

## Performance Characteristics

### Single Word vs Batch Processing

```mermaid
gantt
    title Single Word Analysis Timing
    dateFormat X
    axisFormat %s

    section Word Command
    Pinyin + Definition    :0, 1
    Parallel AI Calls     :1, 4
    Audio Lookup          :4, 5
    Formatting + Display  :5, 6

    section Parse-Pleco (per word)
    Pinyin + Definition    :0, 1
    Parallel AI + Audio   :1, 4
    Display Results       :4, 5
```

**Optimization Notes:**
- Single word analysis optimized for immediate feedback
- AI providers called in parallel to minimize latency
- Audio lookup happens after AI analysis (not blocking)
- Terminal formatting is lightweight and fast

## Configuration Examples

### Basic Usage
```bash
# Minimal analysis with default providers
./zh-learn.sh word 学习

# AI-powered analysis with specific providers
./zh-learn.sh word 学习 \
  --example-provider deepseek-chat \
  --explanation-provider qwen-max \
  --decomposition-provider glm-4-flash
```

### Advanced Configuration
```bash
# Full AI analysis with custom model
./zh-learn.sh word 学习 \
  --example-provider openrouter \
  --model claude-3-sonnet \
  --explanation-provider openrouter \
  --model gpt-4 \
  --audio-provider qwen-tts \
  --raw
```

### Provider Dependencies
- **API Keys Required**: OPENAI_API_KEY, DEEPSEEK_API_KEY, DASHSCOPE_API_KEY, etc.
- **Environment Setup**: Provider-specific configuration via environment variables
- **Fallback Strategy**: Dummy providers for testing without API access

This comprehensive flow demonstrates how the word command provides immediate, detailed analysis of Chinese vocabulary through a flexible provider system optimized for both speed and extensibility.
