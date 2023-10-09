#!/bin/bash
NUMCOUNT=$1
METHOD=$2
ISRANDOMPARAM=$3

urlText() {
  if [[ $ISRANDOMPARAM != "yes" ]]; then
    echo "foobar"
    return
  fi

  randText() {
    cat /dev/random | head -c 6 | sha1sum | head -c 10
  }
  short=$(randText)
  long=$(randText)

  echo "?short=$short&long=$long"
}

export -f urlText
export METHOD
export ISRANDOMPARAM

seq 1 $NUMCOUNT | xargs -P4 -I{} bash -c 'URLTEXT=$(urlText); curl -s -o /dev/null -X $METHOD "http://localhost:8070/$URLTEXT"'