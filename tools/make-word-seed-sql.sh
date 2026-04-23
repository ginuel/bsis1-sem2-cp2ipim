#!/bin/bash

REPO_DIR=$(git rev-parse --show-toplevel)

cd "$REPO_DIR"

# Download words
URL="https://raw.githubusercontent.com/first20hours/google-10000-english/master/google-10000-english-no-swears.txt"
echo "Downloading common English words..."
curl -s $URL > tools/words.txt

# filter words
cat tools/words.txt | sed "s/.*/\L&/" | grep -E "^[a-z]{1,12}$" | sort | uniq > tools/words_filtered.txt

# display words analytics
cat tools/words_filtered.txt | awk '{print length($0)}' | sort -n | uniq -c |
awk '
    BEGIN { print "Length | Count"; print "-------|-------" }
    { printf "%-6s | %s\n", $2, $1; sum += $1 }
    END { print "-------|-------"; print "Total  | " sum }
'

# put sql header
echo "TRUNCATE TABLE Words;" > seed-words.sql
echo "INSERT INTO Words (WordText) VALUES " >> seed-words.sql

# transform words to (), entries
sed "s/.*/('&'),/" tools/words_filtered.txt | sed '$s/,/;/' >> seed-words.sql
