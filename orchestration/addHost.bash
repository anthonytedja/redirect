CWD=$(pwd)
HOSTPORT=$(cat HOSTPORT)
PROXYPORT=8000

# find a new host from HOSTSALL that is not already in HOSTS
new_host=$(grep -v -f HOSTS HOSTSALL | shuf -n 1)

# if $2 was passed in, that is a host we want to copy the data from to the new host and output to dev null
if [ "$2" != "" ]
then
    ssh $new_host "cd '$CWD'; ./orchestration/cloneData.bash $2; ./orchestration/runServerLocal.bash $HOSTPORT >/dev/null"
else
    # start a new server on the new host
    ssh $new_host "cd '$CWD'; ./orchestration/runServerLocal.bash $HOSTPORT >/dev/null"
fi

# If an argument is passed in, we try to replace that host with the new host
if [ "$1" != "" ]
then
    sed -i "s/$1/$new_host/g" HOSTS
    curl "http://localhost:$PROXYPORT?oldhost=$1"
else
    # add it the end of the file on a new line
    sed -i "$ a $new_host" HOSTS
fi

curl "http://localhost:$PROXYPORT?newhost=$new_host"
