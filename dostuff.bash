#!/bin/bash
./orchestration/slopginx.py &
slopginx=($!)
echo "STARTING SLOPGINX $slopginx"

echo "STARTING NEWBERNETES"
./orchestration/newbernetes.bash
echo "KILLED NEWBERNETES"

kill $slopginx
echo "KILLED SLOPGINX"
