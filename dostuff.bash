#!/bin/bash
HOSTPORT=$(cat HOSTPORT)

CWD=$(pwd)
cd orchestration/proxy
make build
java proxy.SimpleProxyServer 8000 > SimpleProxyServer.out 2> SimpleProxyServer.err &
proxy=($!)
cd $CWD

echo "STARTING PROXY"

echo "STARTING NEWBERNETES"
./orchestration/newbernetes.bash $HOSTPORT
echo "KILLED NEWBERNETES"

kill $proxy
echo "KILLED PROXY"
