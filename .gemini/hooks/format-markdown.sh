#!/bin/bash
# Auto-format markdown files after Gemini saves them
# This hook:
# 1. Converts LaTeX math expressions to plain markdown
# 2. Runs markdownlint with --fix to correct formatting issues

# Read JSON input from stdin
INPUT=$(cat)

# Extract file path from tool_input.file_path using jq
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty' 2>/dev/null)

# Exit if no file path found
if [[ -z "$FILE_PATH" ]]; then
    exit 0
fi

# Only process markdown files
if [[ "$FILE_PATH" == *.md ]]; then
    # Check if the file exists
    if [[ -f "$FILE_PATH" ]]; then
        # Fix LaTeX math expressions - convert $$ \text{...} $$ to **...**
        # First, check if file contains LaTeX patterns
        if grep -q '\$\$.*\\text{' "$FILE_PATH" 2>/dev/null; then
            # Create temp file for processing
            TEMP_FILE=$(mktemp)

            # Process the file:
            # 1. Remove $$ delimiters
            # 2. Replace \text{content} with just content
            # 3. Wrap the line in ** for bold
            sed -E '
                # Match lines with $$ ... $$ containing \text{}
                /\$\$.*\\text\{.*\$\$/ {
                    # Remove leading/trailing $$ and spaces
                    s/^\$\$[[:space:]]*//
                    s/[[:space:]]*\$\$$//
                    # Replace \text{content} with content
                    s/\\text\{([^}]*)\}/\1/g
                    # Wrap in bold markers
                    s/^(.*)$/**\1**/
                }
            ' "$FILE_PATH" > "$TEMP_FILE"

            # Replace original with processed
            mv "$TEMP_FILE" "$FILE_PATH"
        fi

        # Run markdownlint with --fix to auto-correct issues
        npx markdownlint-cli2 --fix "$FILE_PATH" 2>/dev/null || true
    fi
fi

exit 0
