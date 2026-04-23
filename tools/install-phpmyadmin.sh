#!/bin/bash

# 1. Get the repository root
REPO_DIR=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$REPO_DIR"

# 2. Install PHP and phpMyAdmin (Ubuntu/Debian)
sudo apt update
sudo apt install -y php php-mysql phpmyadmin
