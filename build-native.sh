#!/bin/bash

# Build native image for ZH Learn application
# Requires GraalVM to be installed and GRAALVM_HOME to be set

set -e

echo "Building ZH Learn native image..."

# Build CLI (and its modules) in one reactor run and produce native image
# Using -pl/-am ensures siblings are built even when invoked from CI or fresh envs.
mvn -B -DskipTests -Pnative -pl zh-learn-cli -am clean package

echo "Native image built successfully: zh-learn-cli/target/zh-learn"
echo "Usage examples:"
echo "  ./zh-learn-cli/target/zh-learn word 汉语 --provider dummy"
echo "  ./zh-learn-cli/target/zh-learn word 学习 --explanation-provider deepseek-chat"
echo ""
echo "Run './zh-learn-cli/target/zh-learn --help' for more options"
