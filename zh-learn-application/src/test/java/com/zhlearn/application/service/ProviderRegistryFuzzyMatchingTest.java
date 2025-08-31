package com.zhlearn.application.service;

import com.zhlearn.infrastructure.dummy.*;
import com.zhlearn.infrastructure.deepseek.DeepSeekExampleProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekExplanationProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekStructuralDecompositionProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExampleProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExplanationProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoStructuralDecompositionProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

class ProviderRegistryFuzzyMatchingTest {

    private ProviderRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ProviderRegistry();
        
        // Register all providers (same as in the actual commands)
        registry.registerDefinitionProvider(new DummyDefinitionProvider());
        registry.registerExampleProvider(new DummyExampleProvider());
        registry.registerExplanationProvider(new DummyExplanationProvider());
        registry.registerPinyinProvider(new DummyPinyinProvider());
        registry.registerStructuralDecompositionProvider(new DummyStructuralDecompositionProvider());

        registry.registerExampleProvider(new DeepSeekExampleProvider());
        registry.registerExplanationProvider(new DeepSeekExplanationProvider());
        registry.registerStructuralDecompositionProvider(new DeepSeekStructuralDecompositionProvider());

        registry.registerExampleProvider(new GPT5NanoExampleProvider());
        registry.registerExplanationProvider(new GPT5NanoExplanationProvider());
        registry.registerStructuralDecompositionProvider(new GPT5NanoStructuralDecompositionProvider());
    }

    @Test
    void testPrefixMatching() {
        // "gpt" should suggest "gpt-5-nano"
        List<String> suggestions = registry.findSimilarProviders("gpt");
        
        assertThat(suggestions)
            .isNotEmpty()
            .contains("gpt-5-nano");
        
        // gpt-5-nano should be the first (best) suggestion
        assertThat(suggestions.get(0)).isEqualTo("gpt-5-nano");
    }

    @Test
    void testPrefixMatchingWithDeepSeek() {
        // "deepseek" should suggest "deepseek-chat"
        List<String> suggestions = registry.findSimilarProviders("deepseek");
        
        assertThat(suggestions)
            .isNotEmpty()
            .contains("deepseek-chat");
        
        // deepseek-chat should be the first suggestion
        assertThat(suggestions.get(0)).isEqualTo("deepseek-chat");
    }

    @Test
    void testContainsMatching() {
        // "nano" should suggest "gpt-5-nano" (contains matching)
        List<String> suggestions = registry.findSimilarProviders("nano");
        
        assertThat(suggestions)
            .isNotEmpty()
            .contains("gpt-5-nano");
    }

    @Test
    void testContainsMatchingWithChat() {
        // "chat" should suggest "deepseek-chat" (contains matching)
        List<String> suggestions = registry.findSimilarProviders("chat");
        
        assertThat(suggestions)
            .isNotEmpty()
            .contains("deepseek-chat");
    }

    @Test
    void testTypoMatching() {
        // "dummi" should suggest "dummy" (Levenshtein distance = 1)
        List<String> suggestions = registry.findSimilarProviders("dummi");
        
        assertThat(suggestions)
            .isNotEmpty()
            .contains("dummy");
        
        // dummy should be the first suggestion for this typo
        assertThat(suggestions.get(0)).isEqualTo("dummy");
    }

    @Test
    void testTypoMatchingVariations() {
        // Test various typos of "dummy"
        assertThat(registry.findSimilarProviders("dummmy")).contains("dummy"); // extra m
        assertThat(registry.findSimilarProviders("dumi")).contains("dummy");   // missing m
        assertThat(registry.findSimilarProviders("dumy")).contains("dummy");   // missing m
    }

    @Test
    void testCaseInsensitiveMatching() {
        // Test case insensitive matching
        assertThat(registry.findSimilarProviders("GPT")).contains("gpt-5-nano");
        assertThat(registry.findSimilarProviders("Dummy")).contains("dummy");
        assertThat(registry.findSimilarProviders("DEEPSEEK")).contains("deepseek-chat");
    }

    @Test
    void testSubsequenceMatching() {
        // Test subsequence matching (abbreviation-like)
        assertThat(registry.findSimilarProviders("dsek")).contains("deepseek-chat"); // d-eep-s-e-ek
        assertThat(registry.findSimilarProviders("g5n")).contains("gpt-5-nano");     // g-pt-5-n-ano
    }

    @Test
    void testNoMatchesForVeryDifferentInput() {
        // Test that very different inputs return empty or no good matches
        List<String> suggestions = registry.findSimilarProviders("xyz123");
        
        // Should either be empty or not contain our known providers as good matches
        assertThat(suggestions).doesNotContain("dummy", "gpt-5-nano", "deepseek-chat");
    }

    @Test
    void testExactMatch() {
        // Exact matches should be handled (though they shouldn't reach fuzzy matching in real usage)
        List<String> suggestions = registry.findSimilarProviders("dummy");
        
        assertThat(suggestions)
            .isNotEmpty()
            .contains("dummy");
        
        // Exact match should be first
        assertThat(suggestions.get(0)).isEqualTo("dummy");
    }

    @Test
    void testMultipleSuggestions() {
        // Test that we can get multiple suggestions
        List<String> suggestions = registry.findSimilarProviders("dum");
        
        assertThat(suggestions)
            .isNotEmpty()
            .contains("dummy");
        
        // Should be limited to reasonable number of suggestions
        assertThat(suggestions).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void testSuggestionOrdering() {
        // Test that better matches come first
        List<String> suggestions = registry.findSimilarProviders("gpt");
        
        if (suggestions.size() > 1) {
            // gpt-5-nano should come before deepseek-chat for "gpt" query
            int gptIndex = suggestions.indexOf("gpt-5-nano");
            int deepSeekIndex = suggestions.indexOf("deepseek-chat");
            
            if (gptIndex >= 0 && deepSeekIndex >= 0) {
                assertThat(gptIndex).isLessThan(deepSeekIndex);
            }
        }
    }

    @Test
    void testEmptyInputHandling() {
        // Test edge case: empty input
        List<String> suggestions = registry.findSimilarProviders("");
        
        // Should handle empty input gracefully (either empty result or all providers)
        assertThat(suggestions).isNotNull();
    }

    @Test
    void testSingleCharacterInput() {
        // Test edge case: single character
        List<String> suggestions = registry.findSimilarProviders("g");
        
        // Should either find matches or return empty, but not crash
        assertThat(suggestions).isNotNull();
        
        // If it finds matches, gpt-5-nano should be included
        if (!suggestions.isEmpty()) {
            assertThat(suggestions).contains("gpt-5-nano");
        }
    }

    @Test
    void testLongInvalidInput() {
        // Test edge case: long invalid input
        List<String> suggestions = registry.findSimilarProviders("this-is-definitely-not-a-provider-name-that-exists");
        
        // Should handle long input gracefully
        assertThat(suggestions).isNotNull();
        
        // Should not contain our known providers as these are very different
        assertThat(suggestions).doesNotContain("dummy", "gpt-5-nano", "deepseek-chat");
    }
}