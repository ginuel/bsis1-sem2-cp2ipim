#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

# 1. Start MariaDB
echo "=== Starting normally ==="
pkill mariadbd 
mariadbd-safe &

# run the file
echo "Starting application..."
./tools/run.sh "$@"
