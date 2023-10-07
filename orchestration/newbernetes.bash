#!/bin/bash
# kubernetes coded by newbs

rm -rf ./server/out
mkdir -p ./server/out

CWD=$(pwd)
HOSTPORT=$(cat HOSTPORT)

start_server() {
    server_pid=$(ssh $1 "cd '$CWD'; ./storage/createDBLocal.bash; ./orchestration/newbernetesLocal.bash $HOSTPORT")
    host_to_pid[$1]=$server_pid
    echo "Started server on host $host, PID: $server_pid"
}

shutdown() {
    for host in "${!host_to_pid[@]}"; do
        ssh $host "kill ${host_to_pid[$host]}"
	    echo "Killed $host"
    done
}

declare -A host_to_pid

# kill all nodes when this script exits
trap "shutdown && exit" INT

for host in $(cat HOSTS); do
    start_server $host
done

echo "CTRL+C to kill all servers..."

# infinite loop
tail -f /dev/null
