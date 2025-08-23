package com.zhlearn.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

import static org.assertj.core.api.Assertions.*;

class TerminalFormatterTest {

    @BeforeAll
    static void setup() {
        // Ensure ANSI is enabled for testing
        System.setProperty("jansi.force", "true");
    }

    @AfterAll
    static void cleanup() {
        TerminalFormatter.shutdown();
    }

    @Test
    void shouldPreserveAnsiColorCodesInBoxWithLongColoredText() {
        // Given: A cyan colored text that will exceed the box width and wrap
        String coloredContent = "\u001B[36mThis is a very long cyan colored text that should wrap across multiple lines and preserve the cyan color throughout all wrapped lines in the box\u001B[0m";
        String title = "Colored Text Test";
        int width = 40;
        
        // When: Box is created
        String boxResult = TerminalFormatter.createBox(title, coloredContent, width);
        
        // Then: All content lines should preserve cyan color
        String[] boxLines = boxResult.split("\n");
        int coloredLineCount = 0;
        
        for (String line : boxLines) {
            // Check content lines (those containing vertical borders but not corner borders)
            if (line.contains("│") && !line.contains("┌") && !line.contains("└")) {
                // Extract content between vertical borders
                int firstPipe = line.indexOf("│");
                int lastPipe = line.lastIndexOf("│");
                if (firstPipe != -1 && lastPipe != -1 && firstPipe != lastPipe) {
                    String content = line.substring(firstPipe + 1, lastPipe);
                    if (content.trim().length() > 0) { // Skip empty lines
                        coloredLineCount++;
                        // Content should contain cyan color code and actual text
                        assertThat(content)
                            .as("All wrapped content lines should preserve cyan color")
                            .contains("\u001B[36m");
                    }
                }
            }
        }
        
        // Ensure we actually had multiple content lines (wrapped)
        assertThat(coloredLineCount)
            .as("Content should wrap to multiple lines")
            .isGreaterThan(1);
    }

    @Test
    void shouldPreserveBoldFormattingInBoxWithLongBoldText() {
        // Given: Bold text that will wrap in the box
        String boldContent = "\u001B[1mThis is a very long bold text that should wrap across multiple lines and preserve the bold formatting throughout all lines in the box\u001B[22m";
        String title = "Bold Text Test";
        int width = 35;
        
        // When: Box is created
        String boxResult = TerminalFormatter.createBox(title, boldContent, width);
        
        // Then: All content lines should preserve bold formatting
        String[] boxLines = boxResult.split("\n");
        int boldLineCount = 0;
        
        for (String line : boxLines) {
            if (line.contains("│") && !line.contains("┌") && !line.contains("└")) {
                int firstPipe = line.indexOf("│");
                int lastPipe = line.lastIndexOf("│");
                if (firstPipe != -1 && lastPipe != -1 && firstPipe != lastPipe) {
                    String content = line.substring(firstPipe + 1, lastPipe);
                    if (content.trim().length() > 0) {
                        boldLineCount++;
                        assertThat(content)
                            .as("All wrapped content lines should preserve bold formatting")
                            .contains("\u001B[1m");
                    }
                }
            }
        }
        
        assertThat(boldLineCount).isGreaterThan(1);
    }

    @Test
    void shouldPreserveMultipleAnsiFormattingInBox() {
        // Given: Text with multiple ANSI codes (bold + cyan) that will wrap
        String styledContent = "\u001B[1m\u001B[36mThis is bold cyan text that is very long and should wrap while preserving both bold and cyan formatting in all lines\u001B[0m";
        String title = "Multi-Style Test";
        int width = 30;
        
        // When: Box is created
        String boxResult = TerminalFormatter.createBox(title, styledContent, width);
        
        // Then: All content lines should preserve both formatting types
        String[] boxLines = boxResult.split("\n");
        int styledLineCount = 0;
        
        for (String line : boxLines) {
            if (line.contains("│") && !line.contains("┌") && !line.contains("└")) {
                int firstPipe = line.indexOf("│");
                int lastPipe = line.lastIndexOf("│");
                if (firstPipe != -1 && lastPipe != -1 && firstPipe != lastPipe) {
                    String content = line.substring(firstPipe + 1, lastPipe);
                    if (content.trim().length() > 0) {
                        styledLineCount++;
                        assertThat(content)
                            .as("All wrapped content lines should preserve both bold and cyan formatting")
                            .contains("\u001B[1m")
                            .contains("\u001B[36m");
                    }
                }
            }
        }
        
        assertThat(styledLineCount).isGreaterThan(1);
    }

    @Test
    void shouldPreserveAnsiFormattingInBoxWithWrappedContent() {
        // Given: Colored content that will wrap inside a box
        String coloredContent = "\u001B[33mThis is yellow text that is intentionally very long to force wrapping inside the box and test if the yellow color continues on the next line\u001B[0m";
        String title = "Test Box";
        int width = 40;
        
        // When: Box is created with wrapping content
        String boxResult = TerminalFormatter.createBox(title, coloredContent, width);
        
        // Then: The wrapped content should preserve yellow color on all lines
        String[] boxLines = boxResult.split("\n");
        
        // Find content lines (skip top border, title line)
        int contentStartIndex = -1;
        int contentLineCount = 0;
        
        for (int i = 0; i < boxLines.length; i++) {
            if (boxLines[i].contains("│") && !boxLines[i].contains("┌") && !boxLines[i].contains("└")) {
                if (contentStartIndex == -1) {
                    contentStartIndex = i;
                }
                contentLineCount++;
            }
        }
        
        assertThat(contentLineCount)
            .as("Content should wrap to multiple lines")
            .isGreaterThan(1);
        
        // Check that each content line preserves the yellow color
        for (int i = contentStartIndex; i < contentStartIndex + contentLineCount; i++) {
            String contentLine = boxLines[i];
            
            // Extract the actual content between the vertical borders
            int firstPipe = contentLine.indexOf("│");
            int lastPipe = contentLine.lastIndexOf("│");
            if (firstPipe != -1 && lastPipe != -1 && firstPipe != lastPipe) {
                String actualContent = contentLine.substring(firstPipe + 1, lastPipe).trim();
                if (!actualContent.isEmpty()) {
                    assertThat(actualContent)
                        .as("Content line %d should contain yellow color code", i - contentStartIndex + 1)
                        .contains("\u001B[33m");
                }
            }
        }
    }
}