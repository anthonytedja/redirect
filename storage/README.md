# Storage

## Overview

The storage module provides a simple interface for storing and retrieving data from a persistent storage. Run the `createDB.bash` script to setup the storage in the `/virtual/$USER/URLShortner` directory on hosts found in the `hosts` file.

```bash
$ ./createDB.bash
```

Add an optional number argument to specify the number of records to populate the database with.

```bash
$ ./createDB.bash 100
```