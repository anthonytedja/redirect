PROXYPORT=$1
JAVA="/opt/jdk-20.0.1/bin/java"
OUTDIR="./out"

cd orchestration/proxy
rm -rf $OUTDIR
mkdir -p $OUTDIR
make build > /dev/null

$JAVA proxy.SimpleProxyServer $PROXYPORT > $OUTDIR/SimpleProxyServer.out 2> $OUTDIR/SimpleProxyServer.err &
echo $! >&1