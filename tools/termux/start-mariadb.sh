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
# Termux manual backgrounding
mariadbd-safe &

password=""
[[ "$DB_PASS" ]] && password="-p$DB_PASS"

# Wait for MariaDB to be ready
until mysqladmin -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" $password ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB..."
    sleep 1
done

# --- 2. Setup phpMyAdmin ---
port=$(shuf -i 8000-9999 -n 1)
host="127.0.0.1" # Force IP to bypass socket path issues
PMA_DIR="$PREFIX/share/phpmyadmin"

if [ ! -d "$PMA_DIR" ]; then
    echo "ERROR: phpMyAdmin not found. Install with: pkg install phpmyadmin"
    exit 1
fi

cd "$PMA_DIR" || exit

# Create the config.inc.php
cat <<EOF > config.inc.php
<?php
\$i = 1;
\$cfg['Servers'][\$i]['auth_type'] = 'config';
\$cfg['Servers'][\$i]['host'] = '$host';
\$cfg['Servers'][\$i]['port'] = '$DB_PORT';
\$cfg['Servers'][\$i]['connect_type'] = 'tcp';
\$cfg['Servers'][\$i]['user'] = '$DB_USER';
\$cfg['Servers'][\$i]['password'] = '$DB_PASS';
\$cfg['Servers'][\$i]['AllowNoPassword'] = true;
\$cfg['blowfish_secret'] = '32_char_random_secret_for_termux_pma';
EOF

# --- 3. Launch via termux-open-url ---
URL="http://$host:$port/index.php?route=/database/sql&db=$DB_NAME"
echo "Launching: $URL"
termux-open-url "$URL"

# --- 4. Start PHP Server ---
php -S $host:$port

