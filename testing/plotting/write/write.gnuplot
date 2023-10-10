#!/usr/bin/gnuplot --persist

stats './testing/plotting/write/writeTest.tsv.fmt' using 1 nooutput

padding = 0.05 * (STATS_max - STATS_min)

set xrange [STATS_min - padding : STATS_max + padding]

set term png
set output './testing/plotting/writeTest.png'

set xlabel "Timestamp when request was sent (truncated to last 4 digits)"
set ylabel "Total response time in ms"

set title "writeTest Performance"
plot "./testing/plotting/write/writeTest.tsv.fmt" using 1:3