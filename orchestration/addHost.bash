CWD=$(pwd)
HOSTPORT=$(cat HOSTPORT)

# remove the host from the list of hosts and replace it with a new host from HOSTSALL that is not already in HOSTS on the same line as the old host
new_host=$(grep -v -f HOSTS HOSTSALL | shuf -n 1)
# replace the old host with the new host in the same spot in HOSTS with sed
sed -i "s/$1/$new_host/g" HOSTS

# TODO: Combine this with newbernetes
# start a new server on the new host
server_pid=$(ssh $new_host "cd '$CWD'; ./storage/createDBLocal.bash; ./orchestration/newbernetesLocal.bash $HOSTPORT")
