#!/bin/bash

# Build native image for ZH Learn application
# Requires GraalVM to be installed and GRAALVM_HOME to be set

set -e

echo "Building ZH Learn native image..."

set -u

# Use the same local Maven repository as the CI build step if available
# Prefer $MAVEN_REPO_LOCAL, then GitHub Actions' $GITHUB_WORKSPACE/.m2
MAVEN_REPO_LOCAL_ARG=""
if [ -n "${MAVEN_REPO_LOCAL:-}" ]; then
  MAVEN_REPO_LOCAL_ARG="-Dmaven.repo.local=$MAVEN_REPO_LOCAL"
elif [ -n "${GITHUB_WORKSPACE:-}" ]; then
  MAVEN_REPO_LOCAL_ARG="-Dmaven.repo.local=$GITHUB_WORKSPACE/.m2"
fi

# Build native image for the CLI module only. Dependencies are resolved from the
# prior reactor install (same repo path), avoiding aggregator plugin execution.
(
  cd zh-learn-cli
  mvn -B -DskipTests $MAVEN_REPO_LOCAL_ARG -Pnative package
)

echo "Native image built successfully: zh-learn-cli/target/zh-learn"
echo "Usage examples:"
echo "  ./zh-learn-cli/target/zh-learn word 汉语 --provider dummy"
echo "  ./zh-learn-cli/target/zh-learn word 学习 --explanation-provider deepseek-chat"
echo ""
echo "Run './zh-learn-cli/target/zh-learn --help' for more options"
