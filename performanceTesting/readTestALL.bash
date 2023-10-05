#!/bin/bash

echo "Running all read tests..."
printf "NUM (LIM) \tSECONDS\n"

./readTest.bash 100 1.0
./readTest.bash 200 1.0
./readTest.bash 400 1.0
./readTest.bash 800 1.0
./readTest.bash 1600 2.0
./readTest.bash 4000 4.0
