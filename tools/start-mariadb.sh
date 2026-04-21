#!/bin/bash

echo "=== Starting normally ==="
pkill mariadbd 
mariadbd-safe &

port=8080

pkill php


termux-open-url "http://localhost:$port/?server=127.0.0.1&username=root&error_stops=1&db=fishdadb&sql="
 
php -S localhost:$port
