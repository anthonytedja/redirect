#!/bin/bash
echo "Running ab with $1 requests"
ab -n $1 -c 4 -u /dev/null -g writeTest.tsv "http://localhost:8000/?short=abc&long=def"