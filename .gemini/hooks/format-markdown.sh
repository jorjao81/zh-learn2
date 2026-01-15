#!/bin/bash
# Auto-format markdown files after Gemini saves them
# This hook runs markdownlint with --fix and checks for LaTeX notation

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

        # Check for LaTeX notation - these should use Unicode instead
        # Pattern matches $\command$ style LaTeX (e.g., $\times$, $\to$)
        LATEX_MATCHES=$(grep -nE '\$\\[a-zA-Z]+\$' "$FILE_PATH" 2>/dev/null || true)

        if [[ -n "$LATEX_MATCHES" ]]; then
            echo "ERROR: LaTeX notation detected in $FILE_PATH"
            echo "Please use Unicode symbols instead of LaTeX:"
            echo ""
            echo "  \$\\to\$, \$\\rightarrow\$ → use: →"
            echo "  \$\\leftarrow\$           → use: ←"
            echo "  \$\\times\$               → use: ×"
            echo "  \$\\checkmark\$           → use: ✓"
            echo "  \$\\approx\$              → use: ≈"
            echo "  \$\\neq\$                 → use: ≠"
            echo ""
            echo "Found LaTeX at:"
            echo "$LATEX_MATCHES"
            exit 1
        fi
    fi
fi

exit 0
