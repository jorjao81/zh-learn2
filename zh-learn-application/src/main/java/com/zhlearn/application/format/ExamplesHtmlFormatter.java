package com.zhlearn.application.format;

import com.zhlearn.domain.model.Example;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Formats Example usages into HTML suitable for both CLI (converted to ANSI) and Anki export.
 */
public final class ExamplesHtmlFormatter {
    private ExamplesHtmlFormatter() {}

    public static String format(Example example) {
        if (example == null || example.usages() == null || example.usages().isEmpty()) {
            // Still render phonetic series if present
            return formatPhoneticSeriesOnly(example);
        }

        StringBuilder html = new StringBuilder();

        // Group usages by context (meaning + pinyin)
        Map<String, List<Example.Usage>> grouped = new LinkedHashMap<>();
        for (Example.Usage usage : example.usages()) {
            String context = usage.context();
            if (context == null) context = "";
            grouped.computeIfAbsent(context, k -> new ArrayList<>()).add(usage);
        }

        for (Map.Entry<String, List<Example.Usage>> entry : grouped.entrySet()) {
            String context = entry.getKey();
            List<Example.Usage> list = entry.getValue();

            if (!context.isEmpty()) {
                html.append("<h4>").append(context).append("</h4>\n");
            }
            html.append("<ul>\n");
            for (Example.Usage u : list) {
                html.append("<li class=\"example\">\n");
                html.append("<span class=\"hanzi\">").append(nz(u.sentence())).append("</span>\n");
                html.append("<span class=\"pinyin\">").append(nz(u.pinyin())).append("</span>\n");
                if (u.translation() != null && !u.translation().isBlank()) {
                    html.append("<span class=\"translation\">- ")
                        .append(u.translation()).append("</span>\n");
                }
                if (u.breakdown() != null && !u.breakdown().isBlank()) {
                    html.append("<span class=\"breakdown\">")
                        .append(prefixBreakdown(u.breakdown()))
                        .append("</span>\n");
                }
                html.append("</li>\n");
            }
            html.append("</ul>\n\n");
        }

        // Append phonetic series if present
        String series = formatPhoneticSeriesOnly(example);
        if (!series.isBlank()) {
            if (html.length() > 0 && html.charAt(html.length() - 1) != '\n') html.append('\n');
            html.append(series);
        }

        return html.toString().trim();
    }

    private static String formatPhoneticSeriesOnly(Example example) {
        if (example == null || example.phoneticSeries() == null || example.phoneticSeries().isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder();
        html.append("<h4>Phonetic series</h4>\n<ul>\n");
        for (Example.SeriesItem item : example.phoneticSeries()) {
            html.append("<li class=\"example\">\n");
            html.append("<span class=\"hanzi\">").append(nz(item.hanzi())).append("</span>\n");
            html.append("<span class=\"pinyin\">").append(nz(item.pinyin())).append("</span>\n");
            if (item.meaning() != null && !item.meaning().isBlank()) {
                html.append("<span class=\"translation\">- ")
                    .append(item.meaning()).append("</span>\n");
            }
            html.append("</li>\n");
        }
        html.append("</ul>\n");
        return html.toString();
    }

    private static String prefixBreakdown(String text) {
        String t = text.trim();
        if (t.toLowerCase().startsWith("breakdown:")) {
            // Remove "breakdown:" prefix if present
            return t.substring(10).trim();
        }
        return t;
    }

    private static String nz(String s) { return s == null ? "" : s; }

}

