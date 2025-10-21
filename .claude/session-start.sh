#!/bin/bash
# Session start hook for zh-learn2 repository
# Installs and verifies: Java 24, Maven 3.8+, FFmpeg

echo "=== ZH Learn Session Start Hook ==="
echo "Checking and installing dependencies..."

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to get Java version (handles both Java 21 and 24+ format)
get_java_version() {
    local version_output=$(java -version 2>&1 | head -n 1)
    if [[ $version_output =~ \"([0-9]+)\.([0-9]+)\.([0-9]+)\" ]]; then
        echo "${BASH_REMATCH[1]}"
    elif [[ $version_output =~ \"([0-9]+)\" ]]; then
        echo "${BASH_REMATCH[1]}"
    else
        echo "0"
    fi
}

# Initialize SDKMAN if available
init_sdkman() {
    if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
        source "$HOME/.sdkman/bin/sdkman-init.sh"
        return 0
    fi
    return 1
}

# Check and install Java 24
echo ""
echo "1. Checking Java version..."

# Try to initialize SDKMAN first
init_sdkman || true

if command_exists java; then
    JAVA_VERSION=$(get_java_version)
    echo "   Current Java version: $JAVA_VERSION"

    if [ "$JAVA_VERSION" -lt 24 ]; then
        echo "   Java 24 required. Attempting installation via SDKMAN..."

        # Install SDKMAN if not present
        if [ ! -d "$HOME/.sdkman" ]; then
            echo "   Installing SDKMAN..."
            if curl -s "https://get.sdkman.io" | bash; then
                init_sdkman
                echo "   ✓ SDKMAN installed"
            else
                echo "   ⚠ SDKMAN installation failed (network issue?)"
                echo "   Please install Java 24 manually or ensure network connectivity"
            fi
        else
            init_sdkman
        fi

        # Try to install Java 24 if SDKMAN is available
        if command_exists sdk; then
            echo "   Installing Java 24..."
            if sdk install java 24-open 2>/dev/null || sdk use java 24-open 2>/dev/null; then
                echo "   ✓ Java 24 configured via SDKMAN"
                echo "   Note: Run 'source ~/.sdkman/bin/sdkman-init.sh' to use Java 24 in your shell"
            else
                echo "   ⚠ Java 24 installation failed"
            fi
        fi
    else
        echo "   ✓ Java $JAVA_VERSION is installed"
    fi
else
    echo "   ⚠ Java not found"
    echo "   Please install Java 24 manually or run this script again with network connectivity"
fi

# Check Maven
echo ""
echo "2. Checking Maven..."
if command_exists mvn; then
    MVN_VERSION=$(mvn --version 2>&1 | head -n 1 | awk '{print $3}')
    echo "   ✓ Maven $MVN_VERSION is installed"

    # Verify Maven version is 3.8+
    MVN_MAJOR=$(echo $MVN_VERSION | cut -d. -f1)
    MVN_MINOR=$(echo $MVN_VERSION | cut -d. -f2)
    if [ "$MVN_MAJOR" -lt 3 ] || ([ "$MVN_MAJOR" -eq 3 ] && [ "$MVN_MINOR" -lt 8 ]); then
        echo "   ⚠ Maven 3.8+ is required (found $MVN_VERSION)"
    fi
else
    echo "   ⚠ Maven not found"
    echo "   Attempting to install via apt..."
    if apt-get update -qq 2>/dev/null && apt-get install -y maven 2>/dev/null; then
        echo "   ✓ Maven installed"
    else
        echo "   ⚠ Maven installation failed (network issue or permissions?)"
        echo "   Please install Maven 3.8+ manually"
    fi
fi

# Check and install FFmpeg
echo ""
echo "3. Checking FFmpeg (optional, for audio normalization)..."
if command_exists ffmpeg; then
    FFMPEG_VERSION=$(ffmpeg -version 2>&1 | head -n 1 | awk '{print $3}')
    echo "   ✓ FFmpeg $FFMPEG_VERSION is installed"
else
    echo "   FFmpeg not found. Attempting installation..."
    if apt-get update -qq 2>/dev/null && apt-get install -y ffmpeg 2>/dev/null; then
        echo "   ✓ FFmpeg installed"
    else
        echo "   ⚠ FFmpeg installation failed (network issue or permissions?)"
        echo "   FFmpeg is optional - audio normalization will fall back to file copy"
        echo "   You can install it later with: sudo apt-get install ffmpeg"
    fi
fi

# Summary
echo ""
echo "=== Dependency check complete ==="
echo ""
echo "Summary:"
if command_exists java; then
    echo "  Java:   $(java -version 2>&1 | head -n 1)"
else
    echo "  Java:   ⚠ Not found"
fi

if command_exists mvn; then
    echo "  Maven:  $(mvn --version 2>&1 | head -n 1)"
else
    echo "  Maven:  ⚠ Not found"
fi

if command_exists ffmpeg; then
    echo "  FFmpeg: $(ffmpeg -version 2>&1 | head -n 1)"
else
    echo "  FFmpeg: ⚠ Not installed (optional)"
fi

echo ""

# Check if Java 24 is available via SDKMAN
if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    init_sdkman
    if command_exists sdk; then
        CURRENT_JAVA=$(sdk current java 2>&1 | grep "Using java" | awk '{print $4}')
        if [[ "$CURRENT_JAVA" == *"24"* ]]; then
            echo "✓ Java 24 is configured via SDKMAN"
            echo "  Run: source ~/.sdkman/bin/sdkman-init.sh"
            echo ""
        fi
    fi
fi

echo "Ready to build and run zh-learn!"
echo "  Build: mvn clean package"
echo "  Run:   ./zh-learn.sh word 学习"
