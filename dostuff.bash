#!/bin/bash
HOSTPORT=$(cat HOSTPORT)
PROXYPORT=8000

echo "STARTING PROXY"
proxy=$(./orchestration/proxy/runProxyLocal.bash $PROXYPORT)

echo "STARTING NEWBERNETES"
./orchestration/newbernetes.bash $HOSTPORT
echo "KILLED NEWBERNETES"

kill $proxy
echo "KILLED PROXY"
