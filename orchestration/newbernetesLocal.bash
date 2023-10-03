HOSTPORT=$1

JAVA="/opt/jdk-20.0.1/bin/java"
HOSTNAME=$(hostname)
OUTDIR="./orchestration/out"
DB_PATH="jdbc:sqlite:/virtual/$USER/URLShortner/data.db"

nohup $JAVA -classpath ".:storage/sqlite-jdbc-3.39.3.0.jar" URLShortner.java $HOSTPORT $DB_PATH > $OUTDIR/$HOSTNAME.out 2> $OUTDIR/$HOSTNAME.err </dev/null &
echo $! >&1
