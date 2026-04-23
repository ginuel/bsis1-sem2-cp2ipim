#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

pkg install php phpmyadmin

cd $PREFIX/share/phpmyadmin
cp config.sample.inc.php config.inc.php
cat << "EOF" >> config.inc.php
$cfg['Servers'][$i]['host'] = '127.0.0.1';
$cfg['Servers'][$i]['compress'] = false;
$cfg['Servers'][$i]['AllowNoPassword'] = true;
EOF
php -S 127.0.0.1:8080

