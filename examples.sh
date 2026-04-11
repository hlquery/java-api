#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

cd "$SCRIPT_DIR"

make compile

for class in \
    hlquery.Example \
    hlquery.examples.Collections \
    hlquery.examples.Documents \
    hlquery.examples.Search
do
    printf '\n== Running %s ==\n' "$class"
    java -cp lib/json.jar:bin "$class"
done
