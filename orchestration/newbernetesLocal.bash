HOSTPORT=$1

JAVA="/opt/jdk-20.0.1/bin/java"
HOSTNAME=$(hostname)
OUTDIR="./orchestration/out"

nohup $JAVA URLShortner.java $HOSTPORT > $OUTDIR/$HOSTNAME.out 2> $OUTDIR/$HOSTNAME.err </dev/null &
echo $! >&1
