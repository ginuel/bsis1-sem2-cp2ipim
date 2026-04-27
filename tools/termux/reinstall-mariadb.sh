#!/data/data/com.termux/files/usr/bin/bash

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

# In Termux, we don't use sudo. 
# We use the $PREFIX variable to point to the correct internal storage.

echo "=== TERMUX CLEANUP: Removing MariaDB traces ==="
# Stop MariaDB if running
killall mariadbd || true

# Termux uses 'pkg' for better dependency handling
pkg uninstall -y mariadb
rm -rf $PREFIX/var/lib/mysql
rm -rf $PREFIX/etc/mysql
rm -rf $PREFIX/var/run/mysqld

echo "=== REINSTALLING: MariaDB and Audio Dependencies ==="
pkg update
# libasound and alsa-utils exist in Termux to support audio
pkg install -y mariadb php 

echo "=== Fixing Directory Structure ==="
# Termux paths are relative to $PREFIX
mkdir -p $PREFIX/etc/mysql $PREFIX/var/lib/mysql $PREFIX/var/run/mysqld

echo "=== Initializing Database ==="
mysql_install_db --user=$(whoami) --datadir=$PREFIX/var/lib/mysql

echo "=== Starting daemon for configuration ==="
# We run it in the background manually as there is no 'service' command in Termux
mariadbd --skip-grant-tables --skip-networking --socket=$PREFIX/var/run/mysqld/mysqld.sock > /dev/null 2>&1 &

echo "Waiting for socket..."
COUNTER=0
while [ ! -S $PREFIX/var/run/mysqld/mysqld.sock ] && [ $COUNTER -lt 20 ]; do
  sleep 1
  let COUNTER=COUNTER+1
done

echo "=== Resetting root user ==="
mariadb -u $(whoami) --socket=$PREFIX/var/run/mysqld/mysqld.sock <<EOF
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS fishdadb;
-- In Termux, the current user is usually the DB owner
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('$DB_PASS');
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

echo "=== Final Restart ==="
killall mariadbd || true
sleep 2

# Start MariaDB normally
mariadbd-safe --datadir=$PREFIX/var/lib/mysql --socket=$PREFIX/var/run/mysqld/mysqld.sock > /dev/null 2>&1 &

echo ""
echo "=== DONE ==="
echo "Note: Termux does not have a 'service' manager by default."
echo "MariaDB is now running in the background."
echo "Java Config: User=root, Pass='$DB_PASS', Host=127.0.0.1"

./tools/migrate.sh
