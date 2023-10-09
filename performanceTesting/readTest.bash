#!/bin/bash

NUMCOUNT=$1
EXPECTED_RUNTIME=$2

# test request, will fail if proxy is down
curl http://localhost:8070/abc
printf "$NUMCOUNT ($EXPECTED_RUNTIME):\t"

(
  cd util
  ./simpleTime.bash ./curlSeq.bash $NUMCOUNT GET no
)
