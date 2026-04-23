#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

echo "=== Starting normally ==="
pkill mariadbd 
mariadbd-safe &

port=8080

pkill php


termux-open-url "http://localhost:$port/?server=127.0.0.1&username=root&error_stops=1&db=fishdadb&sql="
 
php -S localhost:$port tools/index.php
