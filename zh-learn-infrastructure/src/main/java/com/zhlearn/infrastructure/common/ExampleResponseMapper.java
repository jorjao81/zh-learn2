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
            // Parse YAML response
            Map<String, Object> response = yamlMapper.readValue(yamlResponse, Map.class);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) response.get("response");
            
            if (responseList == null || responseList.isEmpty()) {
                log.warn("No examples found in response");
                return new Example(List.of());
            }
            
            List<Example.Usage> allUsages = new ArrayList<>();
            
            // Process each meaning group
            for (Map<String, Object> meaningGroup : responseList) {
                String meaning = (String) meaningGroup.get("meaning");
                List<Map<String, Object>> examples = (List<Map<String, Object>>) meaningGroup.get("examples");
                
                if (examples != null) {
                    for (Map<String, Object> example : examples) {
                        String hanzi = (String) example.get("hanzi");
                        String pinyin = (String) example.get("pinyin");
                        String translation = (String) example.get("translation");
                        
                        // Use meaning as context to preserve the grouping information
                        Example.Usage usage = new Example.Usage(hanzi, pinyin, translation, meaning);
                        allUsages.add(usage);
                    }
                }
            }
            
            return new Example(allUsages);
            
        } catch (Exception e) {
            log.error("Failed to parse YAML response: {}", e.getMessage(), e);
            log.debug("Original response: {}", yamlResponse);
            
            // Return empty example on parse failure
            return new Example(List.of());
        }
    }
}