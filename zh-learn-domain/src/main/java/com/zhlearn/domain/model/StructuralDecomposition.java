package com.zhlearn.domain.model;

import java.util.List;

public record StructuralDecomposition(List<Component> components) {
    public StructuralDecomposition {
        if (components == null || components.isEmpty()) {
            throw new IllegalArgumentException("Structural decomposition components cannot be null or empty");
        }
    }

    public record Component(String character, String meaning, String radical) {
        public Component {
            if (character == null || character.trim().isEmpty()) {
                throw new IllegalArgumentException("Component character cannot be null or empty");
            }
            if (meaning == null || meaning.trim().isEmpty()) {
                throw new IllegalArgumentException("Component meaning cannot be null or empty");
            }
        }
    }
}