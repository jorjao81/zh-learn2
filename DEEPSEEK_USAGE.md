# DeepSeek Explanation Provider Usage

## Overview

The DeepSeek Explanation Provider has been successfully integrated into the ZH Learn application using LangChain4j 1.3.0. It provides AI-powered explanations for Chinese words using DeepSeek's chat model.

## Setup

### 1. Get a DeepSeek API Key

1. Visit [DeepSeek Platform](https://platform.deepseek.com)
2. Sign up for an account
3. Navigate to API Keys section
4. Generate a new API key
5. Copy the API key (it may only be shown once)

### 2. Configure the API Key

Set the environment variable:
```bash
export DEEPSEEK_API_KEY="your-api-key-here"
```

Optional configuration:
```bash
export DEEPSEEK_BASE_URL="https://api.deepseek.com"  # Default, can be customized
```

### 3. Usage Example

The provider will automatically be registered when a valid `DEEPSEEK_API_KEY` is present:

```java
// The provider registry will automatically include DeepSeek
ProviderRegistry registry = new ProviderRegistry();

// Get the DeepSeek explanation provider
Optional<ExplanationProvider> provider = registry.getExplanationProvider("deepseek-deepseek-chat-explanation");

if (provider.isPresent()) {
    ChineseWord word = new ChineseWord("你好");
    Explanation explanation = provider.get().getExplanation(word);
    System.out.println(explanation.explanation());
}
```

## Features

- **Simple Prompt**: Uses a straightforward "Explain X" prompt where X is the Chinese word
- **OpenAI-Compatible**: Uses DeepSeek's OpenAI-compatible API endpoint
- **Automatic Registration**: Automatically registers when API key is available
- **Error Handling**: Graceful error handling with descriptive messages
- **Configurable**: Supports custom base URL and model selection

## Models Supported

- `deepseek-chat` (default) - General-purpose model
- `deepseek-coder` - Specialized for code-related content

## Technical Details

### Implementation

- **Provider**: `DeepSeekExplanationProvider`
- **Package**: `com.zhlearn.infrastructure.deepseek`
- **Dependencies**: LangChain4j 1.3.0 with OpenAI integration
- **Module**: Service provider pattern with Java modules

### Configuration

The provider supports three constructor options:

1. **Default**: Uses environment variables
   ```java
   new DeepSeekExplanationProvider()
   ```

2. **API Key Only**: Uses default settings with custom API key
   ```java
   new DeepSeekExplanationProvider("your-api-key")
   ```

3. **Full Configuration**: Custom API key, base URL, and model
   ```java
   new DeepSeekExplanationProvider("your-api-key", "https://api.deepseek.com", "deepseek-chat")
   ```

## Troubleshooting

### Common Issues

1. **No API Key**: Provider won't register without `DEEPSEEK_API_KEY`
2. **Invalid API Key**: Runtime exception when making API calls
3. **Network Issues**: Check firewall and internet connectivity
4. **Rate Limits**: DeepSeek has usage limits depending on your plan

### Error Messages

- `"Failed to get explanation from DeepSeek API"` - Check API key and network
- `"Failed to register DeepSeek explanation provider"` - Configuration issue

## Cost Considerations

DeepSeek offers competitive pricing:
- Free tier with significant token allowance
- Pay-per-use pricing for additional usage
- Much more economical than other major LLM providers

## Next Steps

Future enhancements could include:
- Support for streaming responses
- Custom prompt templates
- Token usage tracking
- Response caching
- Support for other DeepSeek models (DeepSeek-R1, etc.)