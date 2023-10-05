#!/bin/bash
NUMCOUNT=$1
METHOD=$2
URLPATH=$3

seq 1 $NUMCOUNT | xargs -P4 -I{} curl -s -o /dev/null -X $METHOD http://localhost:8000/$URLPATH
