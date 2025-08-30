#!/bin/bash

# Build native image for ZH Learn application
# Requires GraalVM to be installed and GRAALVM_HOME to be set

set -e

echo "Building ZH Learn native image..."

# Clean and package
mvn clean package -DskipTests

# Build native image (don't clean again to preserve dependencies)
cd zh-learn-cli
mvn -Pnative package -DskipTests

echo "Native image built successfully: zh-learn-cli/target/zh-learn"
echo "Usage examples:"
echo "  ./zh-learn-cli/target/zh-learn word 汉语 --provider dummy"
echo "  ./zh-learn-cli/target/zh-learn word 学习 --explanation-provider deepseek-chat"
echo ""
echo "Run './zh-learn-cli/target/zh-learn --help' for more options"