#!/bin/bash
# usage: <host>
# Check if the specified host is reachable and return output to stdout.

red=$(tput setaf 1)
green=$(tput setaf 2)
normal=$(tput sgr0)

if ping -c 1 -W 2 $1 >/dev/null 2>/dev/null; then
	echo "(${green}o${normal}) host reachable"
else
	echo "(${red}x${normal}) host unreachable"
fi
