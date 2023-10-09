#!/bin/bash
cat HOSTSALL | xargs -I{} -P5 bash -c "echo 'connecting via ssh to {}'; ssh -o StrictHostKeyChecking=accept-new {} 'ls' > /dev/null || true"
echo "DONE CONFIRMING ALL HOSTS"