#!/bin/sh

psql postgresql://postgres:demouser@localhost:5432/i2b2 -f drop_schemas.sql
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f schemas_tables.sql

exit 0