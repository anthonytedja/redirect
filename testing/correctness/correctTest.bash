#!/bin/bash

randText() {
    cat /dev/random | head -c 6 | sha1sum | head -c 10
}

assert() {
    expected="$1"
    actual="$2"
    if [[ "$expected" != "$actual" ]]; then
        echo "TEST FAILED: \"$expected\" != \"$actual\""
        exit 1;
    fi
}

nospace() {
    tr ' ' '_'
}

GET() {
    path=$1
    curl -i "http://localhost:8000/$path" 2>/dev/null
}

PUT() {
    short=$1
    long=$2
    curl -i -X PUT "http://localhost:8000?short=$short&long=$long" 2>/dev/null
}

short=$(randText)
long=$(randText)

# test 404 GET
getResponse=$(GET $short | head -1 | nospace)
assert $getResponse $(echo "HTTP/1.1 404 File Not Found" | nospace)

# test 200 PUT
putResponse=$(PUT $short $long | head -1 | nospace)
assert $putResponse $(echo "HTTP/1.1 200 OK" | nospace)

# test 307 GET
getResponse=$(GET $short | head -1 | nospace)
assert $getResponse $(printf "HTTP/1.1 307 Temporary Redirect\nLocation: $long" | nospace)

echo "All tests passed!"