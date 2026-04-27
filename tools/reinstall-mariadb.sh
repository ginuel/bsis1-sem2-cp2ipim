#!/bin/bash

# Ensure we are in the repo root
REPO_DIR=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$REPO_DIR"

CONFIG="config.properties"
if [ ! -f "$CONFIG" ]; then
    echo "Error: $CONFIG not found!"
    exit 1
fi

get_prop() {
    grep "^${1}=" "$CONFIG" | cut -d'=' -f2 | tr -d '\r'
}

DB_PASS=$(get_prop "db.password")
set -e

echo "=== NUCLEAR UNINSTALL: Removing all MariaDB traces ==="
sudo service mysql stop 2>/dev/null || true
sudo pkill -9 mariadbd || true
sudo pkill -9 mysqld || true

sudo apt purge -y mariadb-server mariadb-client mariadb-common
sudo apt autoremove -y
sudo apt autoclean

# Thorough cleanup
sudo rm -rf /var/lib/mysql /etc/mysql /run/mysqld /var/log/mysql

echo "=== REINSTALLING: MariaDB and Audio Dependencies ==="
sudo apt update
# libasound2-plugins is key for WSLg audio routing
sudo apt install -y mariadb-server php-cli libasound2t64 libasound2-plugins alsa-utils

echo "=== Fixing Directory Structure ==="
# WSL-specific: Ensure the runtime directory exists and is writable
sudo mkdir -p /run/mysqld
sudo chown mysql:mysql /run/mysqld
sudo chmod 755 /run/mysqld

echo "=== Initializing Database (Force Fresh) ==="
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql

echo "=== Starting temporary daemon for configuration ==="
# We run this in the background to set up users
sudo /usr/sbin/mariadbd --skip-grant-tables --skip-networking &
PID=$!

echo "Waiting for socket /run/mysqld/mysqld.sock..."
COUNTER=0
while [ ! -S /run/mysqld/mysqld.sock ] && [ $COUNTER -lt 20 ]; do
  sleep 1
  let COUNTER=COUNTER+1
done

if [ ! -S /run/mysqld/mysqld.sock ]; then
    echo "Error: Socket never appeared. Check 'sudo journalctl -xe' or Windows port 3306 conflicts."
    sudo kill $PID || true
    exit 1
fi

echo "=== Resetting root user ==="
# Using the socket explicitly to bypass networking for now
sudo mariadb -u root --socket=/run/mysqld/mysqld.sock <<EOF
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS fishdadb;
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('$DB_PASS');
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

echo "=== Cleaning up temp daemon ==="
sudo kill $PID
sleep 2

echo "=== Final Restart ==="
# Use the init script for best compatibility in WSL
sudo /etc/init.d/mysql start

# CRITICAL: Permissions for Java/PHP
sudo chmod 777 /run/mysqld/mysqld.sock

echo "=== DONE ==="
echo "Java Config: User=root, Pass='$DB_PASS', Host=127.0.0.1"

./tools/migrate.sh

