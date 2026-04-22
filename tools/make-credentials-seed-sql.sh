#!/bin/bash

# 1. Filter for words that:
#    - Start with a letter: ^[A-Za-z]
#    - Are only alphanumeric: [A-Za-z0-9]*$
#    - Are at least 3 characters long: .{3,}
grep -E '^[A-Za-z][A-Za-z0-9]{2,}$' tools/words_filtered.txt | shuf -n 50 > usernames.txt

# 2. Generate 50 passwords (12 chars, alphanumeric, no spaces)
> passwords.txt
for i in {1..50}; do
    pass=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 12)
    echo "$pass" >> passwords.txt
done

echo "Generated valid usernames (starts with letter, alphanumeric) and passwords."

