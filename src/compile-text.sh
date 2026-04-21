#!/bin/bash

src_dir="$PWD/src"
rm -rf "$src_dir"
mkdir -p "$src_dir"
cp *.java *.properties *.sh *.sql "$src_dir"
# (
# 	cd ../mar20/finproj
# 	mkdir -p "$src_dir/mar20"
# 	cp *.java *.properties *.sh *.sql "$src_dir/mar20"
# )

(
	echo "# Working Directory Tree"
	tree .
	cd "$src_dir"
	rg -n .
) > src.txt
