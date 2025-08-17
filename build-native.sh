#!/bin/bash

# Build native image for ZH Learn application
# Requires GraalVM to be installed and GRAALVM_HOME to be set

set -e

echo "Building ZH Learn native image..."

# Clean and package
mvn clean package -DskipTests

# Build native image
cd zh-learn-cli
mvn -Pnative clean package

echo "Native image built successfully: zh-learn-cli/target/zh-learn"
echo "Usage: ./zh-learn-cli/target/zh-learn word 汉语 --provider dummy-pinyin"