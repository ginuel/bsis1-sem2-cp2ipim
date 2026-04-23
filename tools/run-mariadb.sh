#!/bin/bash

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
until mysqladmin -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB at $DB_HOST:$DB_PORT..."
    sleep 1
done
echo "MariaDB is ready!"

# --- 3. Initialize Database ---
# Note: We use the values extracted above to run the SQL files
if [ -f "fishdadb.sql" ]; then
    echo "Seeding database: $DB_NAME..."
    # Combine the SQL files and pipe them into the mariadb client
    (cat fishdadb.sql; cat seed-words.sql) | $DB_BIN -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME"
    
    # Run the Java Seeder
    ./tools/run.sh GameSeeder
else
    echo "No SQL files found, skipping initialization."
fi

# --- 4. Run the Main Application ---
echo "Starting application..."
./tools/run.sh "$@"
