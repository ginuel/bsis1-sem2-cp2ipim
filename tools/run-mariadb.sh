#!/bin/bash

until mysqladmin -hlocalhost -P3306 -uroot -proot ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB..."
    sleep 1
done
echo "MariaDB is ready!"

mariadb -hlocalhost -P3306 -uroot -proot < <(
	cat down.sql
	cat up.sql
	cat seed.sql
	cat seed-words.sql
)
./tools/run.sh GameSeeder

./tools/run.sh "$@"
