if [ "$1" == "" ]
then
    echo "Usage: ./orchestration/cloneData.bash <host>"
    exit 1
fi

# clone the db from the specified host to the current host
scp $1:/virtual/$USER/URLShortner/data.db /virtual/$USER/URLShortner/data.db >/dev/null
