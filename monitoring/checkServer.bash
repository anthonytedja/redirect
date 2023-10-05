#!/bin/bash
# usage: <host>
# Check if a program called "URLShortner" is running on the specified node and output result to stdout.

grep_name="[U]RLShortner"
program_name=URLShortner

red=$(tput setaf 1)
green=$(tput setaf 2)
normal=$(tput sgr0)

name=$(ssh $1 "ps aux | grep $grep_name | awk '{print \$14}'" 2>/dev/null)
if [ "$name" = "$program_name" ] || [ "$name" = "$program_name.java" ]
then
	echo "(${green}o${normal}) server live"
else
	echo "(${red}x${normal}) server down"
fi
