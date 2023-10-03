#!/bin/bash

CWD=$(pwd)

for host in `cat HOSTS`
do
	ssh $host "cd \"$CWD\"; ./storage/createDBLocal.bash $1;"
done
