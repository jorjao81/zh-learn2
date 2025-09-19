- never add fallbacks unless explicitly told to do so
- never catch exceptions unless explicitly told to do so. let them bubble up and crash the application.
- if you cannot implement something like instructed, don't implement it differently, let me know
- we are using java modules, remeber that you might need to export and import symbols
- NEVER disable or bypass Java modules to try to make something work. We added modules for a reason.

## Adding New AI Providers

All AI providers are managed through `AIProviderFactory`. There are two patterns:

### Pattern 1: OpenAI-Compatible Providers (Simple)

For providers that work with OpenAI-compatible APIs:

1. **Create a Config class** - Handle API keys and base URLs:
```java
public class NewProviderConfig {
    public static final String API_KEY_ENVIRONMENT_VARIABLE = "NEW_PROVIDER_API_KEY";
    public static final String DEFAULT_BASE_URL = "https://api.newprovider.com/v1";

    public static String getApiKey() {
        return readKey(API_KEY_ENVIRONMENT_VARIABLE);
    }

    public static String getBaseUrl() {
        return readKey(BASE_URL_ENVIRONMENT_VARIABLE, DEFAULT_BASE_URL);
    }
}
```

2. **Add cases to AIProviderFactory** - Add to all three factory methods:
```java
case "new-model" -> {
    requireAPIKey("NEW_PROVIDER_API_KEY", providerName);
    var config = createProviderConfig(
        NewProviderConfig.getApiKey(),
        NewProviderConfig.getBaseUrl(),
        "new-model",
        ExampleProviderConfig.templatePath(),
        ExampleProviderConfig.examplesDirectory(),
        ExampleProviderConfig.responseMapper(),
        providerName,
        "Failed to get examples from NewProvider (new-model)"
    );
    yield new ConfigurableExampleProvider(config, providerName, "NewProvider AI");
}
```

### Pattern 2: Custom Providers (Advanced)

For providers needing custom HTTP handling (like Zhipu):

1. **Create a custom provider class** like `ZhipuChatModelProvider`
2. **Use delegate pattern** in AIProviderFactory:
```java
case "custom-model" -> {
    requireAPIKey("CUSTOM_API_KEY", providerName);
    var config = createProviderConfig(...);
    var delegate = new CustomChatModelProvider<>(config);
    yield new ConfigurableExampleProvider(delegate::process, providerName, "Custom AI");
}
```

### Steps for Both Patterns:

1. Add the new provider to all three methods: `createExampleProvider()`, `createExplanationProvider()`, `createDecompositionProvider()`
2. Update the error messages to include the new provider name
3. Test with dummy providers first, then with real API credentials

## Adding New Audio Providers

Audio providers are simpler - they're instantiated directly in MainCommand:

1. **Implement AudioProvider interface**:
```java
public class NewAudioProvider implements AudioProvider {
    @Override
    public String getName() { return "new-audio"; }

    @Override
    public String getDescription() { return "New audio service"; }

    @Override
    public ProviderType getType() { return ProviderType.AUDIO; }

    @Override
    public List<Path> getAudio(Hanzi word, Pinyin pinyin) {
        // Implementation here
    }
}
```

2. **Add to MainCommand constructor**:
```java
this.audioProviders = List.of(
    new AnkiPronunciationProvider(),
    new QwenAudioProvider(),
    new ForvoAudioProvider(),
    new NewAudioProvider()  // Add here
);
```

3. **Test thoroughly** - Audio providers are used in interactive audio selection
