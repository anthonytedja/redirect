#!/bin/bash

./testing/performance/writeTest.bash 4000
./testing/performance/readTest.bash 4000

./testing/plotting/formatTsv.bash ./testing/plotting/write/writeTest.tsv
gnuplot -p ./testing/plotting/write/write.gnuplot

./testing/plotting/formatTsv.bash ./testing/plotting/read/readTest.tsv
gnuplot -p ./testing/plotting/read/read.gnuplot