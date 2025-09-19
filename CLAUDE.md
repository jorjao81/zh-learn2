- never add fallbacks unless explicitly told to do so
- never catch exceptions unless explicitly told to do so. let them bubble up and crash the application.
- if you cannot implement something like instructed, don't implement it differently, let me know
- we are using java modules, remeber that you might need to export and import symbols
- NEVER disable or bypass Java modules to try to make something work. We added modules for a reason.

## Adding New AI Providers

When adding new AI providers, follow the simplified pattern instead of creating multiple classes:

1. **Reuse existing infrastructure**: Use `GenericChatModelProvider<T>` instead of creating new provider classes
2. **Create config factories**: Create classes like `DashScopeConfig` with static factory methods
3. **Instantiate directly**: In `MainCommand` constructor, use `new GenericChatModelProvider<>(ConfigFactory.forType(model))`
4. **Update selection logic**: Modify `determineAIProvider()` and `ProvidersCommand` to handle model variants
5. **Test thoroughly**: Ensure all provider variants work before committing

Example pattern:
```java
// Config factory
public static ProviderConfig<Example> forExamples(String modelName) {
    return new ProviderConfig<>(apiKey, baseUrl, modelName, ...);
}

// Direct instantiation
this.exampleProvider = new GenericChatModelProvider<>(DashScopeConfig.forExamples("qwen3-max"));
```

This keeps the codebase simple while supporting multiple AI models.
