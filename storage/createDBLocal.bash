#!/bin/bash

mkdir -p /virtual/$USER/URLShortner
rm -f /virtual/$USER/URLShortner/data.db
sqlite3 /virtual/$USER/URLShortner/data.db < schema.sql

javac Populate.java
java -classpath ".:sqlite-jdbc-3.39.3.0.jar" Populate.java "jdbc:sqlite:/virtual/$USER/URLShortner/data.db" $1
