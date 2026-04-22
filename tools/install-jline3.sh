#!/bin/bash

# 1. Create the lib directory if it doesn't exist
mkdir -p lib

# 2. Define versions and base URL
JLINE_VERSION="3.26.1"
BASE_URL="https://repo1.maven.org/maven2/org/jline"

echo "Downloading JLine3 dependencies to /lib..."

# 3. Download the Terminal JAR (The API)
curl -L -o lib/jline-terminal-${JLINE_VERSION}.jar \
    "${BASE_URL}/jline-terminal/${JLINE_VERSION}/jline-terminal-${JLINE_VERSION}.jar"

# 4. Download the Native JAR (The Windows/Linux native hooks)
curl -L -o lib/jline-native-${JLINE_VERSION}.jar \
    "${BASE_URL}/jline-native/${JLINE_VERSION}/jline-native-${JLINE_VERSION}.jar"

echo "Download complete!"
ls -l lib

