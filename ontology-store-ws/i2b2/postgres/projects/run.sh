#!/bin/sh

psql postgresql://postgres:demouser@localhost:5432/postgres -f users.sql
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f schemas_tables.sql
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f actdx_projects.sql
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f actpcori_projects.sql
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f actomp_projects.sql

exit 0