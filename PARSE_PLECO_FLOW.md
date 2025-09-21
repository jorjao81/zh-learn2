# Parse-Pleco Command Flow Documentation

## Overview

The `parse-pleco` command processes Pleco export files (TSV format) and analyzes each Chinese word through the complete zh-learn analysis pipeline. It supports both sequential and parallel processing modes, with configurable providers for different analysis components.

## Command Structure

```
./zh-learn.sh parse-pleco <file> [options]
```

### Key Options
- `--parallel-threads`: Number of threads for parallel processing (default: 10)
- `--disable-parallelism`: Force sequential processing
- `--limit`: Limit number of words to process
- `--export-anki`: Export results to Anki-compatible TSV file
- Various provider options for customizing analysis components

## Processing Flow

### High-Level Flow

```mermaid
flowchart TD
    A[Start parse-pleco] --> B[Parse Pleco TSV File]
    B --> C[Create Provider Instances]
    C --> D{Parallelism Enabled?}
    D -->|Yes| E[Parallel Processing Mode]
    D -->|No| F[Sequential Processing Mode]
    E --> G[Audio Selection Phase]
    F --> G
    G --> H{Export to Anki?}
    H -->|Yes| I[Export to TSV]
    H -->|No| J[Complete]
    I --> J
```

## File Parsing Phase

```mermaid
flowchart LR
    A[Pleco TSV File] --> B[PlecoExportParser]
    B --> C[Parse Each Line]
    C --> D[Extract Hanzi]
    C --> E[Convert Pinyin Tones]
    C --> F[Extract Definition]
    D --> G[PlecoEntry]
    E --> G
    F --> G
    G --> H[List of Entries]
```

**Components:**
- **PlecoExportParser**: Handles TSV parsing with tab delimiters
- **PinyinToneConverter**: Converts numbered pinyin (e.g., "ni3 hao3") to tone marks (e.g., "nǐ hǎo")
- **PlecoEntry**: Record containing hanzi, pinyin, and definition

## Provider Setup Phase

```mermaid
flowchart TD
    A[Provider Configuration] --> B[Create AI Providers]
    A --> C[Create Dictionary Providers]
    B --> D[ExampleProvider]
    B --> E[ExplanationProvider]
    B --> F[StructuralDecompositionProvider]
    B --> G[DefinitionFormatterProvider]
    C --> H[PinyinProvider]
    C --> I[DefinitionProvider]
    A --> J[AudioProvider]
    D --> K[WordAnalysisService]
    E --> K
    F --> K
    G --> K
    H --> K
    I --> K
    J --> K
```

**Special Handling:**
- Dictionary providers (`pleco-export`) use the parsed Pleco data directly
- AI providers require API keys and make external calls
- Audio providers handle pronunciation file retrieval

## Processing Modes

### Sequential Processing Mode

```mermaid
sequenceDiagram
    participant Main as Main Thread
    participant WAS as WordAnalysisService
    participant Providers as AI Providers
    participant Audio as AudioOrchestrator
    participant UI as InteractiveAudioUI

    loop For each word
        Main->>WAS: getCompleteAnalysis(word)
        WAS->>Providers: Call providers sequentially
        Providers-->>WAS: Return analysis components
        WAS-->>Main: Complete WordAnalysis
        Main->>Main: Print results immediately
        Main->>Audio: Get pronunciation candidates
        Main->>UI: Run audio selection
        UI-->>Main: Selected audio file
        Main->>Main: Update WordAnalysis with audio
    end
```

**Characteristics:**
- One word processed at a time
- Immediate display of results
- Lower memory usage
- Predictable resource consumption

### Parallel Processing Mode

```mermaid
sequenceDiagram
    participant Main as Main Thread
    participant Executor as Thread Pool
    participant WAS as WordAnalysisService
    participant Providers as AI Providers
    participant Audio as AudioOrchestrator

    Main->>Executor: Submit all words as CompletableFutures

    par Word 1 Analysis + Audio
        Executor->>WAS: getCompleteAnalysis(word1)
        WAS->>Providers: Parallel provider calls
        WAS->>Audio: candidatesFor(word1) [Parallel]
        Providers-->>WAS: Return analysis components
        Audio-->>WAS: Return audio candidates
        WAS-->>Executor: WordWithAudioCandidates1
        Executor-->>Main: Display result1 (when ready)
    and Word 2 Analysis + Audio
        Executor->>WAS: getCompleteAnalysis(word2)
        WAS->>Providers: Parallel provider calls
        WAS->>Audio: candidatesFor(word2) [Parallel]
        Providers-->>WAS: Return analysis components
        Audio-->>WAS: Return audio candidates
        WAS-->>Executor: WordWithAudioCandidates2
        Executor-->>Main: Display result2 (when ready)
    and Word N Analysis + Audio
        Executor->>WAS: getCompleteAnalysis(wordN)
        WAS->>Providers: Parallel provider calls
        WAS->>Audio: candidatesFor(wordN) [Parallel]
        Providers-->>WAS: Return analysis components
        Audio-->>WAS: Return audio candidates
        WAS-->>Executor: WordWithAudioCandidatesN
        Executor-->>Main: Display resultN (when ready)
    end

    Main->>Main: All analysis + audio candidates complete
    Main->>Main: Interactive audio selection with pre-generated candidates
```

**Characteristics:**
- Multiple words processed simultaneously
- Results displayed as they complete (out of order)
- Higher throughput for large datasets
- Increased memory and API usage

## Word Analysis Pipeline Detail

### ParallelWordAnalysisService Internal Flow

```mermaid
flowchart TD
    A[getCompleteAnalysis called] --> B[Get Definition synchronously]
    A --> C[Get Pinyin synchronously]
    B --> D[Launch Parallel Tasks]
    C --> D

    D --> E[CompletableFuture: Structural Decomposition]
    D --> F[CompletableFuture: Examples with Definition]
    D --> G[CompletableFuture: Explanation]
    D --> H[CompletableFuture: Audio Candidates via AudioOrchestrator]

    E --> I[Wait for all futures]
    F --> I
    G --> I
    H --> I

    I --> J[Combine results into WordWithAudioCandidates]
```

**Why Definition and Pinyin are synchronous:**
- Other providers often depend on these results
- Examples provider needs definition text
- Audio provider needs pinyin for pronunciation lookup
- Fast providers (especially dictionary-based ones)

**Audio Candidate Generation Details:**
- AudioOrchestrator manages parallel calls to all audio providers
- Each provider (Anki, Forvo, Qwen TTS, Tencent TTS) runs concurrently
- Results include pre-downloaded and normalized audio files
- Exponential backoff retry for rate-limited providers (HTTP 429)

## Audio Selection Phase

```mermaid
flowchart TD
    A[WordWithAudioCandidates] --> B{Pre-generated Candidates Available?}
    B -->|No| C[Skip Audio Selection]
    B -->|Yes| D[PrePlayback Processing]
    D --> E{Playable Candidates?}
    E -->|No| F[Skip Audio Selection]
    E -->|Yes| G[InteractiveAudioUI]
    G --> H[User Selects Audio]
    H --> I[Update WordAnalysis with Audio Path]
    C --> J[Original WordAnalysis]
    F --> J
    I --> J
```

**Interactive Audio UI Flow:**
1. Use pre-generated audio candidates from parallel processing
2. Display available pronunciation options with provider descriptions
3. Allow user to preview audio files (already downloaded and normalized)
4. User makes selection or skips
5. Selected audio path added to WordAnalysis

**Performance Benefits:**
- No waiting for audio downloads during selection
- All audio files pre-normalized for consistent playback
- Immediate preview capability
- Parallel generation reduces overall processing time

## Data Structures

### Core Data Flow

```mermaid
flowchart LR
    A[PlecoEntry] --> B[Hanzi]
    B --> C[WordAnalysisService]
    C --> D[WordWithAudioCandidates]
    D --> E{Audio Selection}
    E --> F[Updated WordAnalysis]
    F --> G[Anki Export]
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
    }

    class WordWithAudioCandidates {
        +WordAnalysis analysis
        +List~PronunciationCandidate~ audioCandidates
    }

    class PronunciationCandidate {
        +Path audioFile
        +String description
        +String providerName
    }

    class PlecoEntry {
        +String hanzi
        +String pinyin
        +String definitionText
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

    WordWithAudioCandidates --> WordAnalysis
    WordWithAudioCandidates --> PronunciationCandidate
```

## Performance Characteristics

### Parallel vs Sequential Comparison

```mermaid
gantt
    title Processing Time Comparison (100 words)
    dateFormat X
    axisFormat %s

    section Sequential
    Word 1 (AI + Audio)     :0, 4
    Word 2 (AI + Audio)     :4, 8
    Word 3 (AI + Audio)     :8, 12
    Word 4 (AI + Audio)     :12, 16
    Audio Selection         :16, 18

    section Parallel (4 threads) - Old Architecture
    Words 1-4 (AI only)     :0, 3
    Words 5-8 (AI only)     :3, 6
    Audio Generation        :6, 12
    Audio Selection         :12, 14

    section Parallel (4 threads) - New Architecture
    Words 1-4 (AI + Audio) :0, 4
    Words 5-8 (AI + Audio) :4, 8
    Audio Selection        :8, 10
```

**Trade-offs:**
- **Sequential**: Lower resource usage, immediate feedback, predictable timing
- **Parallel (New)**: Optimal throughput with AI + audio in parallel, faster overall completion
- **Parallel (Old)**: Sequential audio bottleneck after AI analysis

**Performance Improvements:**
- Audio downloads no longer block on AI analysis completion
- Overall processing time reduced by ~30-40% for audio-enabled flows
- Better resource utilization with concurrent audio provider calls
- Exponential backoff reduces failed requests for rate-limited services

## Error Handling

```mermaid
flowchart TD
    A[Word Processing] --> B{Provider Call Succeeds?}
    B -->|Yes| C[Continue to Next Provider]
    B -->|No| D[Log Error for Word]
    D --> E[Continue with Next Word]
    C --> F{All Providers Complete?}
    F -->|Yes| G[Word Successfully Analyzed]
    F -->|No| C
    G --> H[Add to Successful Results]
    E --> I[Skip Word in Results]
```

**Error Strategy:**
- Individual word failures don't stop processing
- Errors are logged with word context and timing
- Only successful analyses are included in export
- Final summary shows success/error counts

## Anki Export Process

```mermaid
flowchart LR
    A[Successful WordAnalysis List] --> B[AnkiExporter]
    B --> C[Convert to TSV Format]
    C --> D[Chinese 2 Anki Card Format]
    D --> E[Write to File]

    E --> F[Output: hanzi, pinyin, definition, examples, etc.]
```

**Export Format:**
- Compatible with Anki's "Chinese 2" note type
- Includes all analysis components
- Handles audio file paths if selected
- TSV format for easy import

## Configuration and Provider System

### Provider Selection Logic

```mermaid
flowchart TD
    A[Provider Name] --> B{Is 'pleco-export'?}
    B -->|Yes| C[Create Dictionary Provider]
    B -->|No| D[Create AI Provider]
    C --> E[Use Parsed Pleco Data]
    D --> F[Require API Key]
    F --> G[Create HTTP Client]
    E --> H[Provider Instance]
    G --> H
```

**Provider Types:**
- **Dictionary Providers**: Use local Pleco data (fast, no API calls)
- **AI Providers**: External API calls (slower, require authentication)
- **Audio Providers**: File-based or API-based pronunciation

This comprehensive flow shows how the parse-pleco command efficiently processes large vocabularies while maintaining modularity and user control over the analysis pipeline.