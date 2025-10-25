package com.zhlearn.cli.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ImageSelectionParserTest {

    @Test
    void shouldParseSingleWordSelection() {
        String input = "大象:1,2,3";

        Map<String, List<Integer>> result = ImageSelectionParser.parse(input);

        assertThat(result).hasSize(1);
        assertThat(result.get("大象")).containsExactly(1, 2, 3);
    }

    @Test
    void shouldParseMultipleWordSelections() {
        String input = "大象:1,2 学习:3,4,5";

        Map<String, List<Integer>> result = ImageSelectionParser.parse(input);

        assertThat(result).hasSize(2);
        assertThat(result.get("大象")).containsExactly(1, 2);
        assertThat(result.get("学习")).containsExactly(3, 4, 5);
    }

    @Test
    void shouldHandleSpacesAroundIndices() {
        String input = "大象:1, 2 , 3";

        Map<String, List<Integer>> result = ImageSelectionParser.parse(input);

        assertThat(result.get("大象")).containsExactly(1, 2, 3);
    }

    @Test
    void shouldHandleSingleIndex() {
        String input = "大象:1";

        Map<String, List<Integer>> result = ImageSelectionParser.parse(input);

        assertThat(result.get("大象")).containsExactly(1);
    }

    @Test
    void shouldReturnEmptyMapForNullInput() {
        Map<String, List<Integer>> result = ImageSelectionParser.parse(null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyMapForBlankInput() {
        Map<String, List<Integer>> result = ImageSelectionParser.parse("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyMapForEmptyInput() {
        Map<String, List<Integer>> result = ImageSelectionParser.parse("");

        assertThat(result).isEmpty();
    }
}
