PROXYPORT=8000

# If an argument is passed in, we remove that host
if [ "$1" != "" ]
then
    # use sed to remove the host from HOSTS
    sed -i "/$1/d" HOSTS
    curl -X PUT "http://localhost:$PROXYPORT/?oldhost=$1"
else
    # remove the last host from HOSTS
    old_host=$(tail -n 1 HOSTS)
    sed -i '$ d' HOSTS
    curl -X PUT "http://localhost:$PROXYPORT/?oldhost=$old_host"
fi
