#!/bin/bash
# usage: <optional: interval>
# Monitors the reachability of all nodes and the status of the servers running on each node.
# Monitor every 5 seconds by default.

check_hosts() {
	for host in `cat HOSTS`
	do
		date=$(date +"%Y-%m-%d %T")
		host_status=$(./monitoring/checkHost.bash $host)
		server_status=$(./monitoring/checkServer.bash $host)
		printf "%-20s %-20s %-32s %-32s\n" "$date" "$host" "$host_status" "$server_status"

		# if the host is down, start up a new server on a different host
		if [[ $host_status == *"unreachable"* ]] || [[ $server_status == *"down"* ]]
		then
			./orchestration/addHost.bash $host
		fi
	done
	echo "--------------------------------------------------------------------------------"
}

printf "%-20s %-20s %-21s %-21s\n" "DATE" "HOST" "HOST_STAT" "SERVER_STAT"

while true
do
	check_hosts
	sleep ${1:-5}
done
