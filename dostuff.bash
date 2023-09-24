#!/bin/bash
cd orchestration
./slopginx.py &
slopginx=($!)
echo "STARTING SLOPGINX $slopginx"

cd ../
echo "STARTING NEWBERNETES"
./orchestration/newbernetes.bash
echo "KILLED NEWBERNETES"

kill $slopginx
echo "KILLED SLOPGINX"