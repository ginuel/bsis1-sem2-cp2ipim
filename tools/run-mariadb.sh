#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

# 1. Start MariaDB
./tools/start-mariadb.sh


# run the file
echo "Starting application..."
./tools/run.sh "$@"
