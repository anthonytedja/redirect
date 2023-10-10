#!/bin/bash

TSVFILE=$1

rm -rf ./testing/plotting/format1
rm -rf ./testing/plotting/format2

# remove date column
cat $TSVFILE | xargs -I{} echo {} | sed 's/^[^\t]*\t//g'  >> ./testing/plotting/format1

# format times
cat ./testing/plotting/format1 | xargs -I{} echo {} | sed -r 's/[0-9]*([0-9]{4})/\1/g'  >> ./testing/plotting/format2

cp ./testing/plotting/format2 $1.fmt

rm -rf ./testing/plotting/format1
rm -rf ./testing/plotting/format2
