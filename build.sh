#!/bin/bash

# Build script for CalanityArtifact

echo "Building CalanityArtifact..."
cd "$(dirname "$0")" || exit

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven to build this project."
    exit 1
fi

# Build the project
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Build successful!"
    echo "JAR file location: target/CalanityArtifact-1.0.0.jar"
    echo ""
    echo "To install, copy the JAR to your PaperMC plugins folder:"
    echo "cp target/CalanityArtifact-1.0.0.jar /path/to/server/plugins/"
else
    echo "✗ Build failed!"
    exit 1
fi
