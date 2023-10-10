#!/bin/bash

cd orchestration/proxy
make build
cd ../..

cd server
make build
cd ../..
