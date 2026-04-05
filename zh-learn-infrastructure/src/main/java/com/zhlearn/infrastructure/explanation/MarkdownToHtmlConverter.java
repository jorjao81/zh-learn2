package com.zhlearn.infrastructure.explanation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts a subset of markdown to HTML. Handles headings, bold, italic, and lists. */
public class MarkdownToHtmlConverter {

    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.+?)\\*");
    private static final Pattern ORDERED_LIST_PATTERN = Pattern.compile("^(\\d+)\\.\\s+(.+)$");

    /**
     * Convert markdown text to HTML.
     *
     * @param markdown the markdown content
     * @return HTML string
     */
    public String convert(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        String[] lines = markdown.split("\n");
        List<String> output = new ArrayList<>();
        List<String> listBuffer = new ArrayList<>();
        String listType = null; // "ul" or "ol"

        for (String line : lines) {
            String trimmed = line.trim();

            // Check if this is a list item
            String currentListType = null;
            String itemContent = null;

            if (trimmed.startsWith("- ")) {
                currentListType = "ul";
                itemContent = trimmed.substring(2);
            } else if (trimmed.startsWith("* ") && !trimmed.startsWith("**")) {
                currentListType = "ul";
                itemContent = trimmed.substring(2);
            } else {
                Matcher olMatcher = ORDERED_LIST_PATTERN.matcher(trimmed);
                if (olMatcher.matches()) {
                    currentListType = "ol";
                    itemContent = olMatcher.group(2);
                }
            }

            // Handle indented sub-items as part of current list
            if (currentListType == null && listType != null) {
                if ((trimmed.startsWith("- ") || line.startsWith("  "))
                        && !trimmed.isEmpty()
                        && !trimmed.startsWith("#")) {
                    // Indented content under a list — append to last item
                    if (!listBuffer.isEmpty()) {
                        int last = listBuffer.size() - 1;
                        listBuffer.set(last, listBuffer.get(last) + "<br>" + inlineFormat(trimmed));
                        continue;
                    }
                }
            }

            if (currentListType != null) {
                if (listType != null && !listType.equals(currentListType)) {
                    // Different list type — flush previous
                    flushList(output, listBuffer, listType);
                }
                listType = currentListType;
                listBuffer.add(inlineFormat(itemContent));
                continue;
            }

            // Not a list item — flush any pending list
            if (listType != null) {
                flushList(output, listBuffer, listType);
                listType = null;
            }

            // Headings
            if (trimmed.startsWith("### ")) {
                output.add("<h4>" + inlineFormat(trimmed.substring(4)) + "</h4>");
            } else if (trimmed.startsWith("## ")) {
                output.add("<h3>" + inlineFormat(trimmed.substring(3)) + "</h3>");
            } else if (trimmed.isEmpty()) {
                // Blank line — skip (HTML handles spacing via block elements)
            } else {
                output.add("<p>" + inlineFormat(trimmed) + "</p>");
            }
        }

        // Flush remaining list
        if (listType != null) {
            flushList(output, listBuffer, listType);
        }

        return String.join("\n", output);
    }

    private void flushList(List<String> output, List<String> buffer, String tag) {
        if (buffer.isEmpty()) return;
        output.add("<" + tag + ">");
        for (String item : buffer) {
            output.add("<li>" + item + "</li>");
        }
        output.add("</" + tag + ">");
        buffer.clear();
    }

    /** Apply inline formatting: bold and italic. */
    private String inlineFormat(String text) {
        // Bold first (** before *)
        text = BOLD_PATTERN.matcher(text).replaceAll("<strong>$1</strong>");
        // Then italic
        text = ITALIC_PATTERN.matcher(text).replaceAll("<em>$1</em>");
        return text;
    }
}
