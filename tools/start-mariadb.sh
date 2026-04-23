#!/bin/bash

# Get the root directory
REPO_DIR=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
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
host="127.0.0.1"
PMA_DIR="/usr/share/phpmyadmin"

cd "$PMA_DIR" || exit

# Use 'EOF' to prevent bash from expanding $cfg and $i
# printf then handles the %s injections
CONF_CONTENT=$(printf "$(cat << 'EOF'
<?php
$i = 1;
$cfg['Servers'][$i]['auth_type'] = 'config';
$cfg['Servers'][$i]['host'] = '%s';
$cfg['Servers'][$i]['user'] = '%s';
$cfg['Servers'][$i]['password'] = '%s';
$cfg['Servers'][$i]['AllowNoPassword'] = true;
EOF
)" "$host" "$DB_USER" "$DB_PASS")

# Write to the config file (requires sudo for /usr/share)
echo "$CONF_CONTENT" | sudo tee config.inc.php > /dev/null

echo "Launching: http://$host:$port"
# Open Windows browser from WSL
explorer.exe "http://$host:$port/index.php?route=/database/sql&db=$DB_NAME"

# Start the PHP server
sudo php -S $host:$port

