#!/bin/bash
HOSTPORT=$(cat HOSTPORT)

./storage/createDB.bash

./orchestration/slopginx.py $HOSTPORT &
slopginx=($!)
echo "STARTING SLOPGINX $slopginx"

echo "STARTING NEWBERNETES"
./orchestration/newbernetes.bash $HOSTPORT
echo "KILLED NEWBERNETES"

kill $slopginx
echo "KILLED SLOPGINX"
