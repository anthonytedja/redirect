#!/bin/bash

CWD=$(pwd)/storage;

for host in `cat HOSTS`
do
	ssh $host "cd \"$CWD\"; ./createDBLocal.bash $1;"
done
