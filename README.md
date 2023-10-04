# CSC409 A1 - URL Shortener

## General Notes
### Hosts
All hosts are specified in the `HOSTS` file in the root directory.
### Scripts
The following scripts must be called from the root folder:
 - storage/
   - createDB.bash
   - createDB.bash
 - orchestration/
   - newbernetes.bash
   - slopginx.py
 - monitoring/
   - monitorAll.bash

## Initial Setup
...

## System Orchestration
Run the following from root to start the system:
```bash
$ ./dostuff.bash
```

## API
Sample GET:
```bash
$ curl "http://localhost:8085/arnold" 
```

Sample PUT:
```bash
$ curl -X PUT "http://localhost:8085?short=arnold&long=http://google.com"
```