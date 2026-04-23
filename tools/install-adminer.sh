#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

# Download Adminer (latest version)
wget -O tools/index.php https://www.adminer.org/latest.php

