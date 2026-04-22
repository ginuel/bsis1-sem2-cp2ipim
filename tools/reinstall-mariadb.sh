#!/data/data/com.termux/files/usr/bin/bash

echo "=== Killing all MariaDB processes ==="
pkill -9 mariadbd
pkill -9 mariadb-safe
pkill -9 mysqld
sleep 2

echo "=== Removing MariaDB package ==="
apt remove --purge mariadb -y

echo "=== Nuking ALL data and configs ==="
rm -rf $PREFIX/var/lib/mysql
rm -rf $PREFIX/etc/my.cnf.d
rm -f $PREFIX/etc/my.cnf
rm -f $PREFIX/tmp/mariadb*
rm -f $PREFIX/tmp/mysqld*
rm -f $PREFIX/var/run/mariadbd.pid
rm -f $HOME/.mysql_history
rm -f $HOME/.mariadb_history

echo "=== Reinstalling MariaDB ==="
pkg update
pkg install mariadb php -y

echo "=== Initializing database ==="
mariadb-install-db \
  --auth-root-authentication-method=normal \
  --skip-test-db \
  --user=$(whoami) \
  --basedir=$PREFIX \
  --datadir=$PREFIX/var/lib/mysql

echo "=== Starting with no auth check ==="
mariadbd-safe --skip-grant-tables --skip-networking &

sleep 3

echo "=== Resetting root user ==="
mariadb -u root <<'EOF'
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EXIT;
EOF

echo "=== Restarting normally ==="
pkill mariadbd
sleep 2
mariadbd-safe &

sleep 2
echo ""
echo "=== DONE ==="
echo "Login with: mariadb -u root"
echo "(no password required)"

