#!/bin/bash

echo "=== Starting MariaDB normally ==="
sudo service mysql start

# Generate a random port between 8000 and 9999
port=$(shuf -i 8000-9999 -n 1)

echo "=== Selected Port: $port ==="

# Clean up existing PHP processes (if any)
pkill php || true

# Prepare the URL
URL="http://localhost:$port/?server=127.0.0.1&username=root&error_stops=1&db=fishdadb&sql="

echo "=== Opening Google Chrome ==="
if command -v powershell.exe >/dev/null; then
    # This command tells Windows to find chrome.exe and pass the URL to it
    powershell.exe -Command "Start-Process 'chrome.exe' -ArgumentList '$URL'"
fi

echo "=== Starting PHP Server on port $port ==="
php -S localhost:$port tools/index.php