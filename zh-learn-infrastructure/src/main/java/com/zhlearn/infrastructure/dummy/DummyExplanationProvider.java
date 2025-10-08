package com.zhlearn.infrastructure.dummy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExplanationProvider;

public class DummyExplanationProvider implements ExplanationProvider {

    @Override
    public String getName() {
        return "dummy";
    }

    @Override
    public String getDescription() {
        return "Test provider that returns dummy explanations for development and testing";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DUMMY;
    }

    @Override
    public Explanation getExplanation(Hanzi word) {
        try {
            String htmlContent = loadExampleHtml();
            return new Explanation(htmlContent);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load dummy explanation fixture", e);
        }
    }

    private String loadExampleHtml() throws IOException {
        try (InputStream is =
                getClass()
                        .getResourceAsStream("/single-char/explanation/examples/example-01.html")) {
            if (is == null) {
                throw new IOException("Example HTML file not found");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
