#!/bin/bash

# Get the root of the git repo
REPO_DIR=$(git rev-parse --show-toplevel)
cd "$REPO_DIR"

# 1. Start MariaDB
echo "=== Ensuring MariaDB is running ==="

# In WSL, services often need to be started via the 'service' command 
# if systemd isn't enabled.
sudo service mariadb start

# Alternatively, if you prefer the manual 'safe' background execution:
# pkill mariadbd 
# mariadbd-safe > /dev/null 2>&1 &

# 2. Run the application
echo "Starting application..."
./tools/run.sh "$@"

