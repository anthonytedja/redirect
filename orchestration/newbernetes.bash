#!/bin/bash
# kubernetes coded by newbs

CWD=$(pwd)
HOSTPORT=$(cat HOSTPORT)

start_all() {
    for host in $(cat HOSTS); do
        ssh $host "cd '$CWD'; ./orchestration/runServerLocal.bash $HOSTPORT"
        echo "Started server on host $host"
    done
}

declare -A host_to_pid

# kill all nodes when this script exits
trap "./orchestration/killAllForce.bash && exit" INT

start_all
./orchestration/monitoring/recovery.bash

echo "CTRL+C to kill all servers..."

# infinite loop
tail -f /dev/null
