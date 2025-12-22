package com.zhlearn.infrastructure.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zhlearn.domain.model.Example;

public class ExampleResponseMapper implements Function<String, Example> {

    private static final Logger log = LoggerFactory.getLogger(ExampleResponseMapper.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    public Example apply(String yamlResponse) {
        try {
            // Strip markdown code blocks if present
            String cleanedYaml = MarkdownUtils.stripCodeBlocks(yamlResponse);

            // Parse YAML response
            Map<String, Object> response = yamlMapper.readValue(cleanedYaml, Map.class);
            // Prefer 'words' (new) but accept 'response' (legacy) for backward compatibility
            List<Map<String, Object>> responseList =
                    (List<Map<String, Object>>) response.get("words");
            if (responseList == null) {
                responseList = (List<Map<String, Object>>) response.get("response");
            }

            if (responseList == null || responseList.isEmpty()) {
                throw new IllegalStateException("No examples found in words/response list");
            }

            List<Example.Usage> allUsages = new ArrayList<>();
            List<Example.SeriesItem> seriesItems = parsePhoneticSeries(response);

            // Process each meaning group
            for (Map<String, Object> meaningGroup : responseList) {
                String meaning = (String) meaningGroup.get("meaning");
                String groupPinyin = (String) meaningGroup.get("pinyin");
                List<Map<String, Object>> examples =
                        (List<Map<String, Object>>) meaningGroup.get("examples");

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

                        Example.Usage usage =
                                new Example.Usage(hanzi, pinyin, translation, context, breakdown);
                        allUsages.add(usage);
                    }
                }
            }

            return new Example(allUsages, seriesItems);

        } catch (IllegalStateException e) {
            throw e;
        } catch (JsonProcessingException e) {
            String errorMessage =
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.warn("Failed to parse examples YAML: {}", e.getMessage());
            log.debug("Original response: {}", yamlResponse);
            log.debug("YAML parse exception", e);
            throw new ResponseParsingException("Failed to parse YAML response: " + errorMessage, e);
        }
    }

    private List<Example.SeriesItem> parsePhoneticSeries(Map<String, Object> response) {
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
    }
}
