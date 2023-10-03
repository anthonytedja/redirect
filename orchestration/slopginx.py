#!/bin/python3
# sloppy version of nginx reverse proxy

# we have an array of hosts
# we have a java program that exists on all of them
# we bind our shit to a single port
# as requests come in, we send them to one of the random hosts and return that host's response

import http.server
import requests
import random
import threading
import sys

if len(sys.argv) != 2:
  print("Usage: ./slopginx HOSTPORT")
  exit(1)

BIND = ("localhost", 8000)
HOST_PORT = sys.argv[1]

with open("HOSTS") as f:
    HOSTS = [line.rstrip() for line in f.readlines()]

HOST_CONNS = [requests.Session() for _ in HOSTS]

def choose_host(seed):
    # todo - do something with the seed to get deterministic results
    #host = random.choice(HOSTS)
    #print("host chosen: ", host)
    # return host
    return random.randint(0, len(HOSTS)-1)

def _sendHeaders(headers, target):
    for k, v in headers.items():
        target.send_header(k, v)
    target.end_headers()

def getConnection(host):
    #print(HOSTS, host, HOST_CONNS)
    return HOST_CONNS[host]
    # if host not in HOST_CONNS:
    #     HOST_CONNS[host] = http.client.HTTPConnection(host, HOST_PORT)
    
    # return HOST_CONNS[host]

class DiscountNginx(http.server.BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        return
    def do_GET(self):
        host = choose_host(self.path)

        conn = getConnection(host)
        url = "http://"+HOSTS[host]+":"+str(HOST_PORT)+self.path
        #print(url)
        response = conn.get(url)#self.path)
        # response = conn.getresponse()

        self.send_response(response.status_code)
        _sendHeaders(response.headers, self)
        self.wfile.write(response.content)
    
    def do_PUT(self):
        host = choose_host(self.path)    

        conn = getConnection(host)
        conn.request("PUT", self.path)
        response = conn.getresponse()

        self.send_response(response.status)
        _sendHeaders(response.getheaders(), self)
        self.wfile.write(response.read())


with http.server.ThreadingHTTPServer(BIND, DiscountNginx) as server:
    print(f"Listening on {BIND}")
    server.serve_forever()
