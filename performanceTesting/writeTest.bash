#!/bin/bash

NUMCOUNT=$1
EXPECTED_RUNTIME=$2

# test request, will fail if proxy is down
curl http://localhost:8000/abc
printf "$NUMCOUNT ($EXPECTED_RUNTIME):\t"

randParam() {
  randText() {
    cat /dev/random | head -c 6 | sha1sum | head -c 10
  }
  short=$(randText)
  long=$(randText)
  echo "short=$short&long=$long"
}

(
  cd util
  ./simpleTime.bash ./curlSeq.bash $NUMCOUNT PUT "$(randParam)"
)
