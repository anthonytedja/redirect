HOSTPORT=$1

JAVA="/opt/jdk-20.0.1/bin/java"
HOSTNAME=$(hostname)
OUTDIR="./out"
DB_PATH="jdbc:sqlite:/virtual/$USER/URLShortner/data.db"

# server configuration
IS_VERBOSE=True
CACHE_SIZE=1
WRITE_BUFFER_SIZE=1000
NUM_THREADS=4
SLEEP_DURATION=60000


if [ ! -f /virtual/$USER/URLShortner/data.db ]; then
    ./storage/createDBLocal.bash 10
fi

cd server
rm -rf $OUTDIR
mkdir -p $OUTDIR
#make build

nohup $JAVA -classpath ".:./storage/sqlite-jdbc-3.39.3.0.jar" server.URLShortnerOptimized $IS_VERBOSE $HOSTPORT $DB_PATH $CACHE_SIZE $WRITE_BUFFER_SIZE $NUM_THREADS $SLEEP_DURATION > $OUTDIR/$HOSTNAME.out 2> $OUTDIR/$HOSTNAME.err </dev/null &
echo $! >&1
