#!/bin/bash

# 1. Create the lib directory if it doesn't exist
mkdir -p lib

# 2. Define the version and download URL
# jBcrypt is currently at 0.4 (stable)
VERSION="0.4"
URL="https://repo1.maven.org/maven2/org/mindrot/jbcrypt/$VERSION/jbcrypt-$VERSION.jar"

echo "Downloading jBcrypt v$VERSION to ./lib..."

# 3. Download the JAR file
# Using -L to follow redirects and -o to specify output location
curl -L $URL -o lib/jbcrypt-$VERSION.jar

if [ $? -eq 0 ]; then
    echo "Success! jBcrypt is now in $(pwd)/lib"
    ls -l lib/jbcrypt-$VERSION.jar
else
    echo "Download failed. Please check your internet connection."
fi

