# Storage

> NOTE: Scripts should be called from root

## Overview

The storage module provides a simple interface for storing and retrieving data from a persistent storage. Our database of choice is SQLite3 and stores URL records with short_code and url_original columns.

Run the `createDBLocal.bash` script to setup the storage in the `/virtual/$USER/URLShortner` directory. Add an optional number argument to specify the number of URL records to populate the database with.

```bash
$ ./storage/createDBLocal.bash 100
Populating 100 records 
```

Run the following command to interact with the database.

```bash
$ sqlite3 /virtual/{USER}/URLShortner/data.db
sqlite >
```
