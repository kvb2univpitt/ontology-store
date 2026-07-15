#!/usr/bin/env sh

DIR=/home/kvb2univpitt/mssql

# change hostname from Docker container name to localhost
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/pm_cell_data.sql

# Project
# ##############################################################################
# create new project schema
sqlcmd -No -H localhost -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2actdata_schema.sql
sqlcmd -No -H localhost -d i2b2actata -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2actdata_table.sql

# create new project
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/project.sql

# User
# ###############################################################################
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/user.sql

# OntologyStore datasource
# ###############################################################################
sqlcmd -No -H localhost -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/ontstore_ds_xml.sql

# i2b2
# ###############################################################################
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2pm.sql
sqlcmd -No -H localhost -d i2b2hive -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2hive.sql

exit 0
