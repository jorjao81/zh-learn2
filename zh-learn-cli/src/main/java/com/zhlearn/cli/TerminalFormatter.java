package com.zhlearn.cli;

import com.zhlearn.domain.model.Example;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class TerminalFormatter {
    
    // Box drawing characters
    private static final String TOP_LEFT = "┌";
    private static final String TOP_RIGHT = "┐";
    private static final String BOTTOM_LEFT = "└";
    private static final String BOTTOM_RIGHT = "┘";
    private static final String HORIZONTAL = "─";
    private static final String VERTICAL = "│";
    
    // Color scheme
    public static class Colors {
        public static final Ansi.Color CHINESE = Ansi.Color.CYAN;
        public static final Ansi.Color PINYIN = Ansi.Color.YELLOW;
        public static final Ansi.Color ENGLISH = Ansi.Color.WHITE;
        public static final Ansi.Color HEADER = Ansi.Color.WHITE;
        public static final Ansi.Color PROVIDER = Ansi.Color.BLACK;
        public static final Ansi.Color SEMANTIC_PATH = Ansi.Color.MAGENTA;
        public static final Ansi.Color BOX = Ansi.Color.BLUE;
    }
    
    // ANSI escape sequences as constants
    private static class AnsiCodes {
        public static final String RESET = "\u001B[0m";
        public static final String BOLD = "\u001B[1m";
        public static final String BOLD_OFF = "\u001B[22m";
        public static final String ITALIC = "\u001B[3m";
        
        // Colors
        public static final String CYAN = "\u001B[36m";
        public static final String YELLOW = "\u001B[33m";
        public static final String WHITE = "\u001B[37m";
        public static final String BRIGHT_WHITE = "\u001B[97m";
        public static final String MAGENTA = "\u001B[35m";
        public static final String BLUE = "\u001B[34m";
        public static final String BLACK = "\u001B[30m";
        public static final String GREEN = "\u001B[32m";
        public static final String RED = "\u001B[31m";
    }
    
    // Helper class to track active ANSI formatting state
    private static class AnsiState {
        private String foregroundColor = null;
        private String backgroundColor = null;
        private boolean bold = false;
        private boolean italic = false;
        private boolean underline = false;
        
        public AnsiState() {}
        
        public AnsiState(AnsiState other) {
            this.foregroundColor = other.foregroundColor;
            this.backgroundColor = other.backgroundColor;
            this.bold = other.bold;
            this.italic = other.italic;
            this.underline = other.underline;
        }
        
        public void reset() {
            foregroundColor = null;
            backgroundColor = null;
            bold = false;
            italic = false;
            underline = false;
        }
        
        public boolean hasActiveFormatting() {
            return foregroundColor != null || backgroundColor != null || bold || italic || underline;
        }
        
        public String generateRestoreCode() {
            if (!hasActiveFormatting()) {
                return "";
            }
            
            StringBuilder restore = new StringBuilder();
            if (foregroundColor != null) {
                restore.append(foregroundColor);
            }
            if (backgroundColor != null) {
                restore.append(backgroundColor);
            }
            if (bold) {
                restore.append(AnsiCodes.BOLD);
            }
            if (italic) {
                restore.append(AnsiCodes.ITALIC);
            }
            if (underline) {
                restore.append("\u001B[4m");
            }
            return restore.toString();
        }
    }
    
    // Extract the active ANSI state at the end of a text string
    private static AnsiState extractAnsiState(String text) {
        AnsiState state = new AnsiState();
        if (text == null || text.isEmpty()) {
            return state;
        }
        
        // Pattern to match ANSI escape sequences
        java.util.regex.Pattern ansiPattern = java.util.regex.Pattern.compile("\u001B\\[[0-9;]*[mK]");
        java.util.regex.Matcher matcher = ansiPattern.matcher(text);
        
        while (matcher.find()) {
            String sequence = matcher.group();
            updateAnsiState(state, sequence);
        }
        
        return state;
    }
    
    // Update ANSI state based on an escape sequence
    private static void updateAnsiState(AnsiState state, String sequence) {
        // Remove the \u001B[ prefix and the m suffix to get the codes
        String codes = sequence.substring(2, sequence.length() - 1);
        
        if (codes.isEmpty() || "0".equals(codes)) {
            state.reset();
            return;
        }
        
        String[] parts = codes.split(";");
        for (String code : parts) {
            try {
                int codeNum = Integer.parseInt(code.trim());
                switch (codeNum) {
                    case 0 -> state.reset();
                    case 1 -> state.bold = true;
                    case 3 -> state.italic = true;
                    case 4 -> state.underline = true;
                    case 22 -> state.bold = false;
                    case 23 -> state.italic = false;
                    case 24 -> state.underline = false;
                    // Foreground colors
                    case 30 -> state.foregroundColor = "\u001B[30m"; // Black
                    case 31 -> state.foregroundColor = "\u001B[31m"; // Red
                    case 32 -> state.foregroundColor = "\u001B[32m"; // Green
                    case 33 -> state.foregroundColor = "\u001B[33m"; // Yellow
                    case 34 -> state.foregroundColor = "\u001B[34m"; // Blue
                    case 35 -> state.foregroundColor = "\u001B[35m"; // Magenta
                    case 36 -> state.foregroundColor = "\u001B[36m"; // Cyan
                    case 37 -> state.foregroundColor = "\u001B[37m"; // White
                    case 97 -> state.foregroundColor = "\u001B[97m"; // Bright White
                    // Background colors
                    case 40 -> state.backgroundColor = "\u001B[40m"; // Black bg
                    case 41 -> state.backgroundColor = "\u001B[41m"; // Red bg
                    case 42 -> state.backgroundColor = "\u001B[42m"; // Green bg
                    case 43 -> state.backgroundColor = "\u001B[43m"; // Yellow bg
                    case 44 -> state.backgroundColor = "\u001B[44m"; // Blue bg
                    case 45 -> state.backgroundColor = "\u001B[45m"; // Magenta bg
                    case 46 -> state.backgroundColor = "\u001B[46m"; // Cyan bg
                    case 47 -> state.backgroundColor = "\u001B[47m"; // White bg
                    case 39 -> state.foregroundColor = null; // Default fg
                    case 49 -> state.backgroundColor = null; // Default bg
                }
            } catch (NumberFormatException e) {
                // Ignore invalid codes
            }
        }
    }
    
    static {
        AnsiConsole.systemInstall();
    }
    
    public static void shutdown() {
        AnsiConsole.systemUninstall();
    }
    
    public static String createBox(String title, String content, int width) {
        int titleDisplayWidth = getDisplayLength(title);
        if (width < titleDisplayWidth + 6) {
            width = titleDisplayWidth + 10;
        }
        
        StringBuilder box = new StringBuilder();
        
        // Top border with title
        box.append(Ansi.ansi().fg(Colors.BOX).a(TOP_LEFT).a(HORIZONTAL).a(" ").bold().a(title).boldOff().a(" "));
        int remainingWidth = width - titleDisplayWidth - 5;  // TOP_LEFT + HORIZONTAL + space + space + TOP_RIGHT = 5
        for (int i = 0; i < remainingWidth; i++) {
            box.append(HORIZONTAL);
        }
        box.append(Ansi.ansi().fg(Colors.BOX).a(TOP_RIGHT).reset().toString());
        box.append("\n");
        
        // Content lines with wrapping
        String[] lines = content.split("\n");
        for (String line : lines) {
            // Wrap long lines
            java.util.List<String> wrappedLines = wrapText(line, width - 4);
            for (String wrappedLine : wrappedLines) {
                box.append(Ansi.ansi().fg(Colors.BOX).a(VERTICAL).reset().toString());
                box.append(" ");
                box.append(wrappedLine);
                
                int padding = width - getDisplayLength(wrappedLine) - 3;
                for (int i = 0; i < padding; i++) {
                    box.append(" ");
                }
                
                box.append(Ansi.ansi().fg(Colors.BOX).a(VERTICAL).reset().toString());
                box.append("\n");
            }
        }
        
        // Bottom border
        box.append(Ansi.ansi().fg(Colors.BOX).a(BOTTOM_LEFT));
        for (int i = 0; i < width - 2; i++) {
            box.append(HORIZONTAL);
        }
        box.append(Ansi.ansi().fg(Colors.BOX).a(BOTTOM_RIGHT).reset().toString());
        box.append("\n");
        
        return box.toString();
    }
    
    public static String formatChineseWord(String characters, String pinyin) {
        return Ansi.ansi()
                .bold().fg(Colors.CHINESE).a(characters).reset()
                .a(" (")
                .fg(Colors.PINYIN).a(pinyin).reset()
                .a(")")
                .toString();
    }
    
    public static String formatProvider(String providerName) {
        return Ansi.ansi()
                .fg(Colors.PROVIDER).a("Provider: ").a(providerName).reset()
                .toString();
    }
    
    public static String formatDefinition(String meaning) {
        return Ansi.ansi().fg(Colors.ENGLISH).a(meaning).reset().toString();
    }
    
    public static String formatExample(String chinese, String pinyin, String english, String context) {
        StringBuilder result = new StringBuilder();
        result.append(Ansi.ansi().fg(Colors.CHINESE).a("Chinese: ").a(chinese).reset()).append("\n");
        result.append(Ansi.ansi().fg(Colors.PINYIN).a("Pinyin:  ").a(pinyin).reset()).append("\n");
        result.append(Ansi.ansi().fg(Colors.ENGLISH).a("English: ").a(english).reset());
        if (context != null && !context.isEmpty()) {
            result.append("\n");
            result.append(Ansi.ansi().fgBright(Colors.PROVIDER).a("Context: ").a(context).reset());
        }
        return result.toString();
    }
    
    public static String formatGroupedExamples(java.util.List<Example.Usage> usages) {
        if (usages == null || usages.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        
        // Group examples by context (meaning + pinyin combination)
        java.util.Map<String, java.util.List<Example.Usage>> groupedUsages = new java.util.LinkedHashMap<>();
        for (Example.Usage usage : usages) {
            String context = usage.context();
            if (context == null) context = "";
            groupedUsages.computeIfAbsent(context, k -> new java.util.ArrayList<>()).add(usage);
        }
        
        boolean firstGroup = true;
        for (java.util.Map.Entry<String, java.util.List<Example.Usage>> entry : groupedUsages.entrySet()) {
            if (!firstGroup) {
                result.append("\n\n");
            }
            firstGroup = false;
            
            String context = entry.getKey();
            java.util.List<Example.Usage> examples = entry.getValue();
            
            // Format header like: "to estimate, assess (gū)"
            if (!context.isEmpty()) {
                result.append(Ansi.ansi().bold().fg(Colors.HEADER).a(context).reset()).append("\n");
            }
            
            // Format examples in list format
            for (Example.Usage example : examples) {
                result.append("• ");
                result.append(Ansi.ansi().bold().fg(Colors.CHINESE).a(example.sentence()).reset());
                result.append(" ");
                result.append(Ansi.ansi().fg(Colors.PINYIN).a(example.pinyin()).reset());
                result.append(" ");
                result.append(Ansi.ansi().fg(Colors.ENGLISH).a(example.translation()).reset());
                
                if (example.breakdown() != null && !example.breakdown().isEmpty()) {
                    result.append("\n  ");
                    result.append(Ansi.ansi().fgBright(Colors.PROVIDER).a("Breakdown: ").a(example.breakdown()).reset());
                }
                result.append("\n");
            }
        }
        
        return result.toString();
    }

    public static String formatExamples(Example example) {
        StringBuilder sb = new StringBuilder();
        String grouped = formatGroupedExamples(example.usages());
        if (!grouped.isEmpty()) {
            sb.append(grouped);
        }
        if (example.phoneticSeries() != null && !example.phoneticSeries().isEmpty()) {
            if (!grouped.isEmpty()) sb.append("\n\n");
            sb.append(Ansi.ansi().bold().fg(Colors.HEADER).a("Phonetic series").reset()).append("\n");
            for (Example.SeriesItem item : example.phoneticSeries()) {
                sb.append("• ");
                sb.append(Ansi.ansi().bold().fg(Colors.CHINESE).a(item.hanzi()).reset());
                if (item.pinyin() != null && !item.pinyin().isBlank()) {
                    sb.append(" ");
                    sb.append(Ansi.ansi().fg(Colors.PINYIN).a(item.pinyin()).reset());
                }
                if (item.meaning() != null && !item.meaning().isBlank()) {
                    sb.append(" ");
                    sb.append(Ansi.ansi().fg(Colors.ENGLISH).a(item.meaning()).reset());
                }
                sb.append("\n");
            }
        }
        // No standalone sentence section
        return sb.toString();
    }
    
    public static String formatStructuralDecomposition(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        
        // Parse HTML with JSoup
        Document doc = Jsoup.parse(html);
        
        // Extract components from HTML
        java.util.List<DecompositionComponent> components = new java.util.ArrayList<>();
        Element ulElement = doc.selectFirst("ul");
        if (ulElement != null) {
            for (Element li : ulElement.select("li")) {
                String type = li.hasClass("semantic") ? "semantic" : "phonetic";
                String hanzi = "";
                String pinyin = "";
                String definition = "";
                
                Element hanziSpan = li.selectFirst("span.hanzi");
                if (hanziSpan != null) hanzi = hanziSpan.text();
                
                Element pinyinSpan = li.selectFirst("span.pinyin");
                if (pinyinSpan != null) pinyin = pinyinSpan.text();
                
                Element definitionSpan = li.selectFirst("span.definition");
                if (definitionSpan != null) definition = definitionSpan.text();
                
                components.add(new DecompositionComponent(type, hanzi, pinyin, definition));
            }
        }
        
        if (components.isEmpty()) {
            return html; // Fallback to original if no components found
        }
        
        return formatComponentBoxes(components);
    }
    
    private static String formatComponentBoxes(java.util.List<DecompositionComponent> components) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                result.append(" + ");
            }
            result.append(formatSimpleComponent(components.get(i)));
        }
        
        return result.toString();
    }
    
    private static String formatSimpleComponent(DecompositionComponent component) {
        String badgeText = component.type.equals("semantic") ? "SEMANTIC" : "PHONETIC";
        String badgeColor = component.type.equals("semantic") ? "\u001B[44m\u001B[97m" : "\u001B[42m\u001B[97m"; // Blue or Green bg, white text
        String badge = badgeColor + " " + badgeText + " " + AnsiCodes.RESET;
        
        String hanzi = Ansi.ansi().bold().fg(Colors.CHINESE).a(component.hanzi).reset().toString();
        String pinyin = Ansi.ansi().fg(Colors.PINYIN).a(component.pinyin).reset().toString();
        String definition = Ansi.ansi().fg(Colors.ENGLISH).a(component.definition).reset().toString();
        
        return badge + " " + hanzi + " " + pinyin + " (" + definition + ")";
    }
    
    
    // Helper class for decomposition components
    private static class DecompositionComponent {
        final String type;
        final String hanzi;
        final String pinyin;
        final String definition;
        
        DecompositionComponent(String type, String hanzi, String pinyin, String definition) {
            this.type = type;
            this.hanzi = hanzi != null ? hanzi : "";
            this.pinyin = pinyin != null ? pinyin : "";
            this.definition = definition != null ? definition : "";
        }
    }
    
    public static String convertHtmlToAnsi(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        
        // Parse HTML with JSoup
        Document doc = Jsoup.parse(html);
        
        // Convert DOM to ANSI formatted text
        StringBuilder result = new StringBuilder();
        convertElementToAnsi(doc.body(), result);
        
        // Clean up excessive whitespace and line breaks
        String output = result.toString();
        output = output.replaceAll("[ \t]+", " ");  // Multiple spaces to single space
        output = output.replaceAll(" *\n *", "\n"); // Remove spaces around line breaks
        output = output.replaceAll("\n{3,}", "\n\n"); // Max 2 consecutive line breaks
        return output.trim(); // Remove leading/trailing whitespace
    }
    
    private static void convertElementToAnsi(Element element, StringBuilder result) {
        for (Node node : element.childNodes()) {
            if (node instanceof TextNode textNode) {
                // Add text content
                result.append(textNode.text());
            } else if (node instanceof Element childElement) {
                String tagName = childElement.tagName().toLowerCase();
                String className = childElement.className();
                
                switch (tagName) {
                    case "h1", "h2", "h3" -> {
                        result.append("\n\n");
                        // Simple text decoration since ANSI bold is showing as literal text
                        result.append("=== ").append(Ansi.ansi().bold().fg(Ansi.Color.WHITE)
                                .a(childElement.text()).reset().toString()).append(" ===");
                        result.append("\n\n");
                    }
                    case "h4", "h5", "h6" -> {
                        result.append("\n\n");
                        result.append(Ansi.ansi().bold().fg(Ansi.Color.WHITE)
                                .a(childElement.text()).reset().toString());
                        result.append("\n\n");
                    }
                    case "p" -> {
                        result.append("\n\n");
                        if ("semantic-evolution-path".equals(className)) {
                            result.append(Ansi.ansi().fg(Colors.SEMANTIC_PATH)
                                    .a(">> ").a(AnsiCodes.ITALIC)
                                    .a(childElement.text()).reset().toString());
                        } else {
                            convertElementToAnsi(childElement, result);
                        }
                        result.append("\n\n");
                    }
                    case "b", "strong" -> {
                        result.append(Ansi.ansi().bold().a(childElement.text()).boldOff().toString());
                    }
                    case "i", "em" -> {
                        result.append(Ansi.ansi().a(AnsiCodes.ITALIC)
                                .a(childElement.text()).reset().toString());
                    }
                    case "span" -> {
                        if ("hanzi".equals(className)) {
                            result.append(Ansi.ansi().bold().fg(Colors.CHINESE)
                                    .a(childElement.text()).reset().toString());
                        } else if ("pinyin".equals(className)) {
                            result.append(" ");
                            result.append(Ansi.ansi().fg(Colors.PINYIN)
                                    .a(childElement.text()).reset().toString());
                        } else if ("translation".equals(className)) {
                            result.append(" ");
                            result.append(Ansi.ansi().fg(Colors.ENGLISH)
                                    .a(childElement.text()).reset().toString());
                        } else if ("breakdown".equals(className)) {
                            result.append("\n  ");
                            result.append(Ansi.ansi().fgBright(Colors.PROVIDER)
                                    .a(childElement.text()).reset().toString());
                        } else {
                            convertElementToAnsi(childElement, result);
                        }
                    }
                    case "br" -> result.append("\n");
                    case "ul" -> {
                        result.append("\n");
                        convertElementToAnsi(childElement, result);
                        result.append("\n");
                    }
                    case "li" -> {
                        // Render list items with bullet and newline
                        result.append("• ");
                        convertElementToAnsi(childElement, result);
                        result.append("\n");
                    }
                    case "div" -> {
                        result.append("\n");
                        convertElementToAnsi(childElement, result);
                        result.append("\n");
                    }
                    default -> {
                        // For other elements, just process their children
                        convertElementToAnsi(childElement, result);
                    }
                }
            }
        }
    }
    
    public static int getTerminalWidth() {
        // Default width, could be enhanced to detect actual terminal width
        return 80;
    }
    
    private static java.util.List<String> wrapText(String text, int maxWidth) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            result.add("");
            return result;
        }
        
        // Extract the initial ANSI state from the beginning of the text
        AnsiState initialState = extractAnsiState(text);
        
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        AnsiState currentState = new AnsiState();
        
        for (String word : words) {
            // Update current state with any ANSI codes in this word
            AnsiState wordState = extractAnsiState(word);
            
            // Check if adding this word would exceed the width
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (getDisplayLength(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
                // Update state with any formatting from this word
                updateStateFromText(currentState, word);
            } else {
                // If the current line is not empty, add it to result and start a new line
                if (!currentLine.isEmpty()) {
                    result.add(currentLine.toString());
                    currentLine = new StringBuilder();
                    
                    // Start the next line with the current formatting state if it has active formatting
                    if (currentState.hasActiveFormatting()) {
                        currentLine.append(currentState.generateRestoreCode());
                    }
                }
                
                // If the word itself is too long, we need to break it
                if (getDisplayLength(word) > maxWidth) {
                    String remainingWord = word;
                    while (getDisplayLength(remainingWord) > maxWidth) {
                        // Find the best break point
                        int breakPoint = findBreakPoint(remainingWord, maxWidth);
                        String part = remainingWord.substring(0, breakPoint);
                        
                        // If we're continuing formatting from previous line, prepend restore code
                        if (currentState.hasActiveFormatting() && currentLine.isEmpty()) {
                            currentLine.append(currentState.generateRestoreCode());
                        }
                        currentLine.append(part);
                        result.add(currentLine.toString());
                        
                        remainingWord = remainingWord.substring(breakPoint);
                        currentLine = new StringBuilder();
                        updateStateFromText(currentState, part);
                    }
                    if (!remainingWord.isEmpty()) {
                        // If we're continuing formatting, prepend restore code
                        if (currentState.hasActiveFormatting() && currentLine.isEmpty()) {
                            currentLine.append(currentState.generateRestoreCode());
                        }
                        currentLine.append(remainingWord);
                        updateStateFromText(currentState, remainingWord);
                    }
                } else {
                    // If we're continuing formatting from previous line, prepend restore code
                    if (currentState.hasActiveFormatting() && currentLine.isEmpty()) {
                        currentLine.append(currentState.generateRestoreCode());
                    }
                    currentLine.append(word);
                    updateStateFromText(currentState, word);
                }
            }
        }
        
        // Add any remaining content
        if (!currentLine.isEmpty()) {
            result.add(currentLine.toString());
        }
        
        return result.isEmpty() ? java.util.List.of("") : result;
    }
    
    // Helper method to update ANSI state from text content
    private static void updateStateFromText(AnsiState state, String text) {
        java.util.regex.Pattern ansiPattern = java.util.regex.Pattern.compile("\u001B\\[[0-9;]*[mK]");
        java.util.regex.Matcher matcher = ansiPattern.matcher(text);
        
        while (matcher.find()) {
            String sequence = matcher.group();
            updateAnsiState(state, sequence);
        }
    }
    
    private static int findBreakPoint(String text, int maxWidth) {
        // Simple approach: break at maxWidth, but could be enhanced
        // to break at word boundaries or avoid breaking ANSI sequences
        int breakPoint = maxWidth;
        
        // Don't break in the middle of ANSI escape sequences
        for (int i = 1; i <= maxWidth && i < text.length(); i++) {
            if (text.charAt(maxWidth - i) == '\u001B') {
                // Found start of ANSI sequence, break before it
                return maxWidth - i;
            }
        }
        
        return Math.min(breakPoint, text.length());
    }
    
    private static int getDisplayLength(String text) {
        if (text == null) return 0;
        
        // Remove ANSI escape sequences to get actual display length
        // Using the same pattern as in our ANSI parsing methods for consistency
        String cleaned = text.replaceAll("\u001B\\[[0-9;]*[mK]", "");
        
        int width = 0;
        for (int i = 0; i < cleaned.length(); ) {
            int codePoint = cleaned.codePointAt(i);
            
            // Check if this is a wide character (CJK characters, emojis, etc.)
            if (isWideCharacter(codePoint)) {
                width += 2;
            } else {
                width += 1;
            }
            
            i += Character.charCount(codePoint);
        }
        
        return width;
    }
    
    private static boolean isWideCharacter(int codePoint) {
        // CJK Unified Ideographs
        if (codePoint >= 0x4E00 && codePoint <= 0x9FFF) return true;
        // CJK Compatibility Ideographs
        if (codePoint >= 0xF900 && codePoint <= 0xFAFF) return true;
        // CJK Unified Ideographs Extension A
        if (codePoint >= 0x3400 && codePoint <= 0x4DBF) return true;
        // CJK Unified Ideographs Extension B
        if (codePoint >= 0x20000 && codePoint <= 0x2A6DF) return true;
        // CJK Unified Ideographs Extension C
        if (codePoint >= 0x2A700 && codePoint <= 0x2B73F) return true;
        // CJK Unified Ideographs Extension D
        if (codePoint >= 0x2B740 && codePoint <= 0x2B81F) return true;
        // CJK Symbols and Punctuation
        if (codePoint >= 0x3000 && codePoint <= 0x303F) return true;
        // Hiragana
        if (codePoint >= 0x3040 && codePoint <= 0x309F) return true;
        // Katakana
        if (codePoint >= 0x30A0 && codePoint <= 0x30FF) return true;
        // Halfwidth and Fullwidth Forms (fullwidth only)
        if (codePoint >= 0xFF01 && codePoint <= 0xFF60) return true;
        if (codePoint >= 0xFFE0 && codePoint <= 0xFFE6) return true;
        
        return false;
    }
    
    // New formatting methods for providers command
    
    public static String formatBoldLabel(String label) {
        return Ansi.ansi().bold().a(label).boldOff().toString();
    }
    
    public static String formatSupportedCapability(String capability) {
        return Ansi.ansi().fg(Ansi.Color.GREEN).a("✓ ").reset().a(capability).toString();
    }
    
    public static String formatUnsupportedCapability(String capability) {
        return Ansi.ansi().fg(Ansi.Color.RED).a("✗ ").reset().a(capability).toString();
    }
    
    public static String formatWarning(String message) {
        return Ansi.ansi().fg(Ansi.Color.YELLOW).a("⚠ ").reset().a(message).toString();
    }
    
    public static String formatProviderDescription(String description) {
        return formatBoldLabel("Description: ") + description;
    }
}
