#!/bin/bash

cd ..

src_dir="$PWD/src"
rm -rf "$src_dir"
mkdir -p "$src_dir"
cp *.java *.properties *.sh *.sql "$src_dir"

(
	echo "# Working Directory Tree"
	tree .
	cd "$src_dir"
	rg -n .
) > src.txt
