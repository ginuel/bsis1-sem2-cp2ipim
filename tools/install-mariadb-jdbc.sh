#!/bin/bash

VERSION=3.5.7
JAR_NAME="mariadb-java-client-$VERSION.jar"
DIR="lib"
URL="https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/$VERSION/$JAR_NAME"

(
	mkdir -p "$DIR"
	cd "$DIR"
	if ! [ -f "$JAR_NAME" ]; then
		curl -L -o "$JAR_NAME" "$URL"
	fi
)
