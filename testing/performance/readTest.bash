#!/bin/bash
echo "Running ab with $1 requests"
ab -n $1 -c 4 -g ./testing/plotting/read/readTest.tsv http://localhost:8000/abc