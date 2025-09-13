package com.zhlearn.infrastructure.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zhlearn.domain.model.Example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExampleResponseMapper implements Function<String, Example> {
    
    private static final Logger log = LoggerFactory.getLogger(ExampleResponseMapper.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    @Override
    public Example apply(String yamlResponse) {
        try {
            // Strip markdown code blocks if present
            String cleanedYaml = stripMarkdownCodeBlocks(yamlResponse);
            
            // Parse YAML response
            Map<String, Object> response = yamlMapper.readValue(cleanedYaml, Map.class);
            // Prefer 'words' (new) but accept 'response' (legacy) for backward compatibility
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get("words");
            if (responseList == null) {
                responseList = (List<Map<String, Object>>) response.get("response");
            }
            
            if (responseList == null || responseList.isEmpty()) {
                log.warn("No examples found in words/response list");
                // Try to still parse phonetic series if present; otherwise return empty
                List<Example.SeriesItem> series = parsePhoneticSeries(response);
                return new Example(List.of(), series);
            }
            
            List<Example.Usage> allUsages = new ArrayList<>();
            List<Example.SeriesItem> seriesItems = parsePhoneticSeries(response);
            
            // Process each meaning group
            for (Map<String, Object> meaningGroup : responseList) {
                String meaning = (String) meaningGroup.get("meaning");
                String groupPinyin = (String) meaningGroup.get("pinyin");
                List<Map<String, Object>> examples = (List<Map<String, Object>>) meaningGroup.get("examples");
                
                // Create context by combining meaning and pinyin: "to estimate, assess (gū)"
                String context = meaning;
                if (groupPinyin != null && !groupPinyin.isEmpty()) {
                    context = meaning + " (" + groupPinyin + ")";
                }
                
                if (examples != null) {
                    for (Map<String, Object> example : examples) {
                        String hanzi = (String) example.get("hanzi");
                        String pinyin = (String) example.get("pinyin");
                        String translation = (String) example.get("translation");
                        String breakdown = (String) example.get("breakdown");
                        
                        Example.Usage usage = new Example.Usage(hanzi, pinyin, translation, context, breakdown);
                        allUsages.add(usage);
                    }
                }
            }
            
            return new Example(allUsages, seriesItems);
            
        } catch (Exception e) {
            log.error("Failed to parse YAML response: {}", e.getMessage(), e);
            log.debug("Original response: {}", yamlResponse);
            
            // Return empty example on parse failure
            return new Example(List.of(), List.of());
        }
    }

    private List<Example.SeriesItem> parsePhoneticSeries(Map<String, Object> response) {
        try {
            Object raw = response.get("phonetic_series");
            if (raw == null) return List.of();
            List<Map<String, Object>> list = (List<Map<String, Object>>) raw;
            List<Example.SeriesItem> result = new ArrayList<>();
            for (Map<String, Object> item : list) {
                if (item == null) continue;
                String hanzi = (String) item.get("hanzi");
                String pinyin = (String) item.get("pinyin");
                String meaning = (String) item.get("meaning");
                if (hanzi != null && !hanzi.isBlank()) {
                    result.add(new Example.SeriesItem(hanzi, pinyin, meaning));
                }
            }
            return result;
        } catch (Exception ex) {
            log.debug("Failed to parse phonetic_series: {}", ex.getMessage());
            return List.of();
        }
    }

    // Standalone sentences not supported
    
    private String stripMarkdownCodeBlocks(String input) {
        if (input == null) {
            return null;
        }
        
        String trimmed = input.trim();
        
        // Check if the input starts with ```yaml or ``` and ends with ```
        if (trimmed.startsWith("```yaml") && trimmed.endsWith("```")) {
            // Remove ```yaml from the start and ``` from the end
            int startIndex = trimmed.indexOf('\n', 7); // Find first newline after ```yaml
            if (startIndex == -1) {
                startIndex = 7; // No newline found, start after ```yaml
            } else {
                startIndex++; // Move past the newline
            }
            int endIndex = trimmed.lastIndexOf("```");
            return trimmed.substring(startIndex, endIndex).trim();
        } else if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            // Generic code block without yaml specifier
            int startIndex = trimmed.indexOf('\n', 3); // Find first newline after ```
            if (startIndex == -1) {
                startIndex = 3; // No newline found, start after ```
            } else {
                startIndex++; // Move past the newline
            }
            int endIndex = trimmed.lastIndexOf("```");
            return trimmed.substring(startIndex, endIndex).trim();
        }
        
        // No markdown code blocks found, return as is
        return input;
    }
}
