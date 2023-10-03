#!/usr/bin/python3

import random, string, subprocess

for i in range(4000):
	request="http://localhost:8000/abc"
	# print(request)
	subprocess.call(["curl", "-X", "GET", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
