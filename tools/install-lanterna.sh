#!/bin/sh

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"


VERSION="3.1.2"
LIB_DIR="./lib"
URL="https://repo1.maven.org/maven2/com/googlecode/lanterna/lanterna/${VERSION}/lanterna-${VERSION}.jar"

# Create lib directory
mkdir -p "$LIB_DIR"
cd "$LIB_DIR"

curl -L -o lanterna.jar "$URL"
echo "Downloaded: lanterna-${VERSION}.jar"

