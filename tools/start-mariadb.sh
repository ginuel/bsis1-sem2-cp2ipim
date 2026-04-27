#!/bin/bash

# Get the root directory
REPO_DIR=$(git rev-parse --show-toplevel)
cd "$REPO_DIR"

# --- 1. Load config.properties ---
CONFIG="config.properties"
get_prop() {
    grep "^${1}=" "$CONFIG" | cut -d'=' -f2 | tr -d '\r'
}

DB_HOST=$(get_prop "db.host")
DB_PORT=$(get_prop "db.port")
DB_USER=$(get_prop "db.user")
DB_PASS=$(get_prop "db.password")
DB_NAME=$(get_prop "db.name")

echo "=== Starting MariaDB Service ==="
sudo service mariadb start

password=""
[[ "$DB_PASS" ]] && password="-p$DB_PASS"

# Wait for MariaDB to be ready
until mysqladmin -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" $password ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB..."
    sleep 1
done

# --- 2. Setup phpMyAdmin ---
port=$(shuf -i 8000-9999 -n 1)
host="127.0.0.1" # Force IP to avoid socket errors
PMA_DIR="/usr/share/phpmyadmin"

cd "$PMA_DIR" || exit

# Create clean config.inc.php 
sudo tee config.inc.php > /dev/null <<EOF
<?php
\$i = 1;
\$cfg['Servers'][\$i]['auth_type'] = 'config';
\$cfg['Servers'][\$i]['host'] = '$host';
\$cfg['Servers'][\$i]['port'] = '$DB_PORT';
\$cfg['Servers'][\$i]['connect_type'] = 'tcp';
\$cfg['Servers'][\$i]['user'] = '$DB_USER';
\$cfg['Servers'][\$i]['password'] = '$DB_PASS';
\$cfg['Servers'][\$i]['AllowNoPassword'] = true;
\$cfg['blowfish_secret'] = 'a8b7c6d5e4f3g2h1i0j9k8l7m6n5o4p3'; 
EOF

# --- 3. Launch Windows Chrome ONLY ---
URL="http://$host:$port/index.php?route=/database/sql&db=$DB_NAME"
CHROME_PATH="/mnt/c/Program Files/Google/Chrome/Application/chrome.exe"

if [ -f "$CHROME_PATH" ]; then
    echo "Launching Chrome: $URL"
    "$CHROME_PATH" "$URL" &
else
    echo "ERROR: Chrome not found at $CHROME_PATH"
    exit 1
fi

# --- 4. Start PHP Server ---
sudo php -S $host:$port

