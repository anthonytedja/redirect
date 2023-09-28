#!/bin/python3
# sloppy version of nginx reverse proxy

# we have an array of hosts
# we have a java program that exists on all of them
# we bind our shit to a single port
# as requests come in, we send them to one of the random hosts and return that host's response

import http.server
import http.client
import random
import threading
import sys

if len(sys.argv) != 2:
  print("Usage: ./slopginx HOSTPORT")
  exit(1)

BIND = ("localhost", 8000)
HOST_PORT = sys.argv[1]

with open("hosts") as f:
    HOSTS = [line.rstrip() for line in f.readlines()]

def choose_host(seed):
    # todo - do something with the seed to get deterministic results
    host = random.choice(HOSTS)
    print("host chosen: ", host)
    return host

def _sendHeaders(headers, target):
    for k, v in headers:
        target.send_header(k, v)
    target.end_headers()

class DiscountNginx(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        host = choose_host(self.path)

        conn = http.client.HTTPConnection(host, HOST_PORT)
        conn.request("GET", self.path)
        response = conn.getresponse()

        self.send_response(response.status)
        _sendHeaders(response.getheaders(), self)
    
    def do_PUT(self):
        host = choose_host(self.path)    

        conn = http.client.HTTPConnection(host, HOST_PORT)
        conn.request("PUT", self.path)
        response = conn.getresponse()

        self.send_response(response.status)
        _sendHeaders(response.getheaders(), self)
        self.wfile.write(response.read())


with http.server.HTTPServer(BIND, DiscountNginx) as server:
    print(f"Listening on {BIND}")
    server.serve_forever()
