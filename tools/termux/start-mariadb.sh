#!/bin/bash

# Get the root directory
REPO_DIR=$(git rev-parse --show-toplevel)
cd "$REPO_DIR"

# --- 1. Load config.properties ---
CONFIG="config.properties"

if [ ! -f "$CONFIG" ]; then
    echo "Error: $CONFIG not found!"
    exit 1
fi

get_prop() {
    grep "^${1}=" "$CONFIG" | cut -d'=' -f2 | tr -d '\r'
}

DB_HOST=$(get_prop "db.host")
DB_PORT=$(get_prop "db.port")
DB_USER=$(get_prop "db.user")
DB_PASS=$(get_prop "db.password")
DB_NAME=$(get_prop "db.name")

echo "=== Starting MariaDB Service ==="
# WSL standard service start
sudo service mariadb start

password=""
[[ "$DB_PASS" ]] && password="-p$DB_PASS"

# Wait for MariaDB to be ready
until mysqladmin -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" $password ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB at $DB_HOST:$DB_PORT..."
    sleep 1
done
echo "MariaDB is ready!"

# --- 2. Setup phpMyAdmin ---
port=$(shuf -i 8000-9999 -n 1)
host="127.0.0.1"
# Standard WSL/Ubuntu phpMyAdmin path
PMA_DIR="/usr/share/phpmyadmin"

if [ ! -d "$PMA_DIR" ]; then
    echo "Error: phpMyAdmin not found at $PMA_DIR. Please install via 'sudo apt install phpmyadmin'"
    exit 1
fi

cd "$PMA_DIR" || exit

# Create a fresh config from sample
sudo cp config.sample.inc.php config.inc.php

# Append the config settings (Note the escaped $ for PHP variables)
CONF_CONTENT=$(printf "$(cat << "EOF"
$cfg['Servers'][$i]['auth_type'] = 'config';
$cfg['Servers'][$i]['host'] = '%s';
$cfg['Servers'][$i]['user'] = '%s';
$cfg['Servers'][$i]['password'] = '%s';
$cfg['Servers'][$i]['AllowNoPassword'] = true;
EOF
)" "$host" "$DB_USER" "$DB_PASS")

echo "$CONF_CONTENT" | sudo tee -a config.inc.php > /dev/null

# --- 3. Launch Google Chrome (Windows) ---
URL="http://$host:$port/index.php?route=/database/sql&db=$DB_NAME"
CHROME_PATH="/mnt/c/Program Files/Google/Chrome/Application/chrome.exe"

echo "Launching Chrome..."
	"$CHROME_PATH" "$URL" &

# --- 4. Start the PHP server ---
echo "Starting PHP server on http://$host:$port"
sudo php -S $host:$port

