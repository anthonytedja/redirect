PROXYPORT=$1
OUTDIR="./out"

cd orchestration/proxy
rm -rf $OUTDIR
mkdir -p $OUTDIR
make build > /dev/null

java proxy.SimpleProxyServer $PROXYPORT > $OUTDIR/SimpleProxyServer.out 2> $OUTDIR/SimpleProxyServer.err &
echo $! >&1