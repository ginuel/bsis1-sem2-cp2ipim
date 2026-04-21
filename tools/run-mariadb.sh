#!/bin/bash

cd ..

until mysqladmin -hlocalhost -P3306 -uroot -proot ping --silent 2>/dev/null; do
    echo "Waiting for MariaDB..."
    sleep 1
done
echo "MariaDB is ready!"

false && {
	mariadb -hlocalhost -P3306 -uroot -proot < <(
		cat fishdadb.sql
		cat seed-words.sql
	)
	./run.sh GameSeeder
}

./run.sh "$@"
