#!/bin/bash
set -x
PROXYPORT=$1
JAVA="/opt/jdk-20.0.1/bin/java"
OUTDIR="./orchestration/proxy/out"

rm -rf $OUTDIR
mkdir -p $OUTDIR
make build -C orchestration/proxy > /dev/null

$JAVA -cp orchestration/proxy proxy.SimpleProxyServer $PROXYPORT > $OUTDIR/SimpleProxyServer.out 2> $OUTDIR/SimpleProxyServer.err &
echo $! >&1