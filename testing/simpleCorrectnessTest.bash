#!/bin/bash

port=8070

curl -X PUT "http://localhost:$port?short=1&long=aaa"
curl -X PUT "http://localhost:$port?short=2&long=aaa"
curl -X PUT "http://localhost:$port?short=3&long=aaa"
curl -X PUT "http://localhost:$port?short=4&long=aaa"
curl -X PUT "http://localhost:$port?short=5&long=aaa"

curl "http://localhost:$port/1"
curl "http://localhost:$port/2"
curl "http://localhost:$port/3"
curl "http://localhost:$port/4"
curl "http://localhost:$port/5"