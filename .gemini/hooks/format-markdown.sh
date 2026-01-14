#!/bin/bash
# Auto-format markdown files after Gemini saves them
# This hook runs markdownlint with --fix to correct formatting issues

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
        # Run markdownlint with --fix to auto-correct issues
        npx markdownlint-cli2 --fix "$FILE_PATH" 2>/dev/null || true
    fi
fi

exit 0
