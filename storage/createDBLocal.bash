#!/bin/bash

mkdir -p /virtual/$USER/URLShortner
rm -f /virtual/$USER/URLShortner/data.db
sqlite3 /virtual/$USER/URLShortner/data.db < storage/schema.sql
JAVA="/opt/jdk-20.0.1/bin/java"
JAVAC="/opt/jdk-20.0.1/bin/javac"

$JAVAC storage/Populate.java
$JAVA -classpath ".:storage/sqlite-jdbc-3.39.3.0.jar" storage/Populate.java "jdbc:sqlite:/virtual/$USER/URLShortner/data.db" $1
