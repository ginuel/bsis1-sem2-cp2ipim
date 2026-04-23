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

# --- 2. Wait for MariaDB ---
# We use the host, port, user, and pass from config
password=
if [[ "$DB_PASS" ]]; then
	password="-p$DB_PASS"
fi

until mysqladmin -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" $password  ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB at $DB_HOST:$DB_PORT..."
    sleep 1
done
echo "MariaDB is ready!"


# --- 3. Initialize Database ---
# Note: We use the values extracted above to run the SQL files
echo "Seeding database: $DB_NAME..."
# Combine the SQL files and pipe them into the mariadb client
(
	cat down.sql
	cat up.sql
	cat seed.sql
	cat seed-words.sql
) | $DB_BIN -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" $password "$DB_NAME"

# Run the Java Seeder
./tools/run.sh GameSeeder
