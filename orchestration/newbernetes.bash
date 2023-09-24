#!/bin/bash
# kubernetes coded by newbs

CWD=$(pwd)

exec_on_host() {
    # idk what the -t -t does but if we don't have it, nodes don't get killed properly
    ssh -t -t "$1" "cd $CWD && java URLShortner.java" &
    nodepid=$!
    nodes+=($nodepid)
    echo "exec on host: $host, node: $nodepid"
}

kill_node() {
    kill $1
    echo "killed node $1"
}

kill_all_nodes() {
    for node in "${nodes[@]}"; do
        kill_node $node
    done
}

declare -a nodes

# kill all nodes when this script exits
trap "kill_all_nodes && exit" INT

for host in $(cat ./orchestration/hosts); do
    exec_on_host $host
done

echo "CTRL+C to kill nodes..."

# infinite loop
tail -f /dev/null