#!/usr/bin/python3

import random, string, subprocess

for i in range(10):
  longResource = "http://"+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(100))
  shortResource = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(20))

  request="http://localhost:8080/?short="+shortResource+"&long="+longResource
  # print(request)
  with open("writeTest.out", "w") as f:
    subprocess.call(["curl", "-X", "PUT", request], stdout=f, stderr=subprocess.DEVNULL)
