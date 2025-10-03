#!/bin/bash

# Build all modules and run end-to-end tests
# This script builds the application and then runs the e2e test suite

set -e

echo "Building ZH Learn and running E2E tests..."

set -u

# Use the same local Maven repository as the CI build step if available
# Prefer $MAVEN_REPO_LOCAL, then GitHub Actions' $GITHUB_WORKSPACE/.m2
MAVEN_REPO_LOCAL_ARG=""
if [ -n "${MAVEN_REPO_LOCAL:-}" ]; then
  MAVEN_REPO_LOCAL_ARG="-Dmaven.repo.local=$MAVEN_REPO_LOCAL"
elif [ -n "${GITHUB_WORKSPACE:-}" ]; then
  MAVEN_REPO_LOCAL_ARG="-Dmaven.repo.local=$GITHUB_WORKSPACE/.m2"
fi

echo "Step 1/2: Building all modules and running unit tests..."
mvn -B $MAVEN_REPO_LOCAL_ARG clean package

echo ""
echo "Step 2/2: Running end-to-end tests..."
mvn -B $MAVEN_REPO_LOCAL_ARG -pl zh-learn-e2e test

echo ""
echo "✓ All builds and tests passed successfully!"
