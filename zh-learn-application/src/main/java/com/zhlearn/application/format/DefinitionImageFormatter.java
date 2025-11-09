package com.zhlearn.application.format;

import java.nio.file.Path;
import java.util.List;

/** Formats definitions with embedded image HTML tags for Anki. */
public class DefinitionImageFormatter {

    public DefinitionImageFormatter() {}

    /**
     * Format definition with embedded image HTML.
     *
     * @param definition the definition text
     * @param imagePaths list of image paths (Anki media files)
     * @return HTML-formatted definition with embedded images
     */
    public String formatWithImages(String definition, List<Path> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return definition;
        }

        StringBuilder html = new StringBuilder();

        // Definition section
        html.append("<div class=\"definition\">\n");
        html.append(escapeHtml(definition));
        html.append("\n</div>\n");

        // Images section
        html.append("<div class=\"images\">\n");
        for (Path imagePath : imagePaths) {
            String filename = imagePath.getFileName().toString();
            html.append("  <img src=\"").append(filename).append("\" class=\"word-image\">\n");
        }
        html.append("</div>");

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
