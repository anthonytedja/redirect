#!/bin/bash

mkdir -p /virtual/$USER/URLShortner
rm -f /virtual/$USER/URLShortner/data.db
sqlite3 /virtual/$USER/URLShortner/data.db < schema.sql
