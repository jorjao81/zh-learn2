package com.zhlearn.infrastructure.ai;

import java.util.List;
import java.util.Map;

public record AiProviderConfig(
    String name,
    String prompt,
    List<String> examples,
    Map<String, String> additionalConfiguration
) {
    public AiProviderConfig {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("AI provider name cannot be null or empty");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("AI provider prompt cannot be null or empty");
        }
        if (examples == null) {
            throw new IllegalArgumentException("AI provider examples cannot be null");
        }
        if (additionalConfiguration == null) {
            throw new IllegalArgumentException("AI provider additional configuration cannot be null");
        }
    }
}