#!/bin/bash

src_dir="$PWD/src"
rm -rf "$src_dir"
mkdir -p "$src_dir"
cp *.java *.properties *.sql "$src_dir"

(
	echo "# Working Directory Tree"
	tree .
	cd "$src_dir"
	rg -n .
) > tools/src.txt
