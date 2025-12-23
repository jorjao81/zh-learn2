# TODO

## Local LLM Response Validator

Use a lightweight local model (Gemma 3 270M) as an "LLM as a judge" to verify AI provider responses actually answer the question asked.

**Use case**: Validate that when requesting a definition, the provider returns a definition (not hallucination, refusal, or off-topic content). Not for quality assessment—just binary verification that the response type matches the request type.

**Why Gemma 3 270M**:
- ~200MB at INT4 quantization, runs locally on any machine
- Optimized for classification/verification tasks
- Fast inference (milliseconds)
- Available via MLX (Apple Silicon), llama.cpp, or Keras

**Fallback**: If 270M proves insufficient for edge cases, step up to Gemma 3 1B/2B (still local-friendly).

**Java integration** (java-llama.cpp):
```xml
<dependency>
    <groupId>de.kherud</groupId>
    <artifactId>llama</artifactId>
    <version>4.1.0</version>
</dependency>
```
llama.cpp supports Gemma natively in GGUF format. Alternative: ONNX Runtime Java (faster but more setup).

## Local TTS Output Validator

Use a Chinese-only speech-to-text model to verify that TTS audio providers actually produce the expected Chinese text.

**Use case**: After TTS generates audio for "学习", run local STT to confirm the audio contains "学习" (not silence, wrong characters, or garbage). Binary validation, not pronunciation quality assessment.

**Why Paraformer-zh**:
- 220M parameters, Chinese-only (no multilingual bloat)
- ~800MB model size, runs on Mac with MPS (Apple Silicon)
- 10x faster than autoregressive models
- Trained on 60k hours of Mandarin

**Java integration** (ONNX Runtime):
```xml
<dependency>
    <groupId>com.microsoft.onnxruntime</groupId>
    <artifactId>onnxruntime</artifactId>
    <version>1.16.0</version>
</dependency>
```
Paraformer exports to ONNX. Requires audio preprocessing (feature extraction) before inference.

**Alternative**: SenseVoice-Small (similar size, 5 languages, also has ONNX export).
