#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

# --- 1. Load config.properties ---
CONFIG="config.properties"

# Function to get property values
get_prop() {
    grep "^${1}=" "$CONFIG" | cut -d'=' -f2 | tr -d '\r'
}

DB_HOST=$(get_prop "db.host")
DB_PORT=$(get_prop "db.port")
DB_USER=$(get_prop "db.user")
DB_PASS=$(get_prop "db.password")
DB_NAME=$(get_prop "db.name")
DB_BIN=$(get_prop "db.mysql-path")

echo "=== Starting normally ==="
pkill mariadbd 
mariadbd-safe &

password=
if [[ "$DB_PASS" ]]; then
	password="-p$DB_PASS"
fi

until mysqladmin -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" $password  ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB at $DB_HOST:$DB_PORT..."
    sleep 1
done
echo "MariaDB is ready!"

port=$(shuf -i 8000-9999 -n 1)
host=127.0.0.1


 
cd $PREFIX/share/phpmyadmin
cp config.sample.inc.php config.inc.php
printf "$(cat << "EOF" 
$cfg['Servers'][$i]['auth_type'] = 'config';
$cfg['Servers'][$i]['host'] = '%s';
$cfg['Servers'][$i]['user'] = '%s';
$cfg['Servers'][$i]['password'] = '%s';
$cfg['Servers'][$i]['AllowNoPassword'] = true;
EOF
)" "$host" "$DB_USER" "$DB_PASS" >> config.inc.php

termux-open-url "http://$host:$port/index.php?route=/database/sql&db=$DB_NAME"

php -S $host:$port

