#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

# --- 1. Load config.properties ---
CONFIG="config.properties"

# Function to get property values
get_prop() {
    grep "^${1}=" "$CONFIG" | cut -d'=' -f2 | tr -d '\r'
}

DB_PASS=$(get_prop "db.password")

# Exit on any error
set -e

echo "=== NUCLEAR UNINSTALL: Removing all MariaDB traces ==="
# Stop services and kill processes
sudo service mysql stop || true
sudo pkill -9 mariadbd || true
sudo pkill -9 mysqld || true

# Purge packages (removes configs and binaries)
sudo apt purge -y mariadb-server mariadb-client mariadb-common
sudo apt autoremove -y
sudo apt autoclean

# Delete remaining directories manually to ensure a fresh start
sudo rm -rf /var/lib/mysql
sudo rm -rf /etc/mysql
sudo rm -rf /run/mysqld
sudo rm -rf /var/log/mysql

echo "=== REINSTALLING: MariaDB and Audio Dependencies ==="
sudo apt update
# Reinstalling MariaDB + PHP + Audio libraries needed for Java Clip class
sudo apt install -y mariadb-server php-cli libasound2t64 libasound2-plugins alsa-utils

echo "=== Fixing Directory Structure ==="
sudo mkdir -p /etc/mysql /var/lib/mysql /run/mysqld
sudo chown -R mysql:mysql /var/lib/mysql /run/mysqld
sudo chmod 755 /run/mysqld

echo "=== Initializing Database (Force Fresh) ==="
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql

echo "=== Starting temporary daemon for configuration ==="
SAFE_BIN=$(which mariadbd-safe || which mysqld_safe)
sudo $SAFE_BIN --skip-grant-tables --skip-networking > /dev/null 2>&1 &

echo "Waiting for socket..."
COUNTER=0
while [ ! -S /run/mysqld/mysqld.sock ] && [ $COUNTER -lt 20 ]; do
  sleep 1
  let COUNTER=COUNTER+1
done

echo "=== Resetting root user (EMPTY PASSWORD) ==="
sudo mariadb -u root --socket=/run/mysqld/mysqld.sock <<EOF
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS fishdadb;
-- Fix for Java: Use native password plugin with empty string
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('$DB_PASS');
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

echo "=== Final Restart ==="
sudo pkill -9 mariadbd || true
sudo pkill -9 mysqld || true
sleep 2

# WSL specific service start
sudo service mysql start

# CRITICAL: Fix permissions so Java/PHP can access the socket without sudo
sudo chmod 777 /run/mysqld/mysqld.sock

echo ""
echo "=== DONE ==="
echo "MariaDB has been completely uninstalled and reinstalled."
echo "Java Config: User=root, Pass='$DB_PASS', Host=127.0.0.1"

./tools/run-mariadb.sh GameDatabase
