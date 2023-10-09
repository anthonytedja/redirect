#!/bin/bash

echo "Running all write tests..."
printf "NUM (LIM) \tSECONDS\n"

./writeTest.bash 100 1.0
./writeTest.bash 200 1.0
./writeTest.bash 400 1.0
./writeTest.bash 800 2.0
./writeTest.bash 1600 4.0
./writeTest.bash 4000 8.0
