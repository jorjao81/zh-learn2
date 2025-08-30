package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.provider.ExplanationProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DummyExplanationProvider implements ExplanationProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public Explanation getExplanation(Hanzi word) {
        try {
            String htmlContent = loadExampleHtml();
            return new Explanation(htmlContent);
        } catch (Exception e) {
            // Fallback to simple text if file loading fails
            return new Explanation(
                "Dummy explanation for " + word.characters() + ": This word has ancient origins and is commonly used in daily conversation. It has cultural significance in Chinese society."
            );
        }
    }
    
    private String loadExampleHtml() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/explanation/examples/example-01.html")) {
            if (is == null) {
                throw new IOException("Example HTML file not found");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}