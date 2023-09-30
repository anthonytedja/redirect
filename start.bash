#!/bin/bash

HOSTPORT=$(cat HOSTPORT)

if [ ! -f /virtual/$USER/URLShortner/data.db ]; then
    ./storage/createDBLocal.bash 10
fi

javac URLShortner.java
java -classpath ".:storage/sqlite-jdbc-3.39.3.0.jar" URLShortner.java $HOSTPORT "jdbc:sqlite:/virtual/$USER/URLShortner/data.db"
