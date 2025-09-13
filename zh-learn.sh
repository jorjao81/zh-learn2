#!/bin/bash

# ZH-Learn - Modular Java Application Launcher
# This script demonstrates true modular execution using --module-path

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLI_TARGET="$SCRIPT_DIR/zh-learn-cli/target"

# Check if application is built
if [ ! -d "$CLI_TARGET/lib" ]; then
    echo "Application not built. Please run: mvn clean package"
    exit 1
fi

# Build module path - this is the key for true modular execution
# Include all dependency JARs and our CLI JAR
MODULE_PATH="$CLI_TARGET/lib:$CLI_TARGET/zh-learn-cli-1.0.0-SNAPSHOT.jar"

# Execute using true Java modules
exec java \
    --module-path "$MODULE_PATH" \
    --enable-native-access=org.fusesource.jansi,ALL-UNNAMED \
    --module com.zhlearn.cli/com.zhlearn.cli.ZhLearnApplication \
    "$@"
