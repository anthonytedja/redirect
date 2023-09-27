#!/bin/bash

program_name=[U]RLShortner

echo "Forcefully shutting down servers across all hosts..."
for host in $(cat hosts)
do
	ssh $host "ps aux | grep $program_name | awk '{print \$2}' | xargs kill -9"
	echo "Shutdown server on $host"
done
