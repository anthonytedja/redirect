#!/bin/bash
JAVA="/opt/jdk-20.0.1/bin/java"
OUTDIR="./orchestration/proxy/out"

# proxy configuration
IS_VERBOSE=True
PROXYPORT=$1
CACHE_SIZE=500
NUM_THREADS=4


rm -rf $OUTDIR
mkdir -p $OUTDIR
make build -C orchestration/proxy > /dev/null

$JAVA -cp orchestration/proxy proxy.SimpleProxyServer $IS_VERBOSE $PROXYPORT $CACHE_SIZE $NUM_THREADS > $OUTDIR/SimpleProxyServer.out 2> $OUTDIR/SimpleProxyServer.err &
echo $! >&1