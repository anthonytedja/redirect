PATH=$1
nohup java URLShortner > /dev/null 2>/dev/null </dev/null &
echo $! >&1
