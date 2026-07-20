#!/usr/bin/env sh

DIR=/home/kvb2univpitt/sqlserver

# change hostname from Docker container name to localhost
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/pm_cell_data.sql

# Project
# ##############################################################################
# create new project schema
sqlcmd -No -H localhost -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2actdata_project_schema.sql
sqlcmd -No -H localhost -d i2b2actata -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2actdata_project_tables.sql
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2actdata_project.sql

# User
# ###############################################################################
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2_user.sql

# OntologyStore datasource
# ###############################################################################
sqlcmd -No -H localhost -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/ontstore_ds_xml.sql
sqlcmd -No -H localhost -d i2b2hive -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/ontstore_i2b2hive.sql
sqlcmd -No -H localhost -d i2b2pm -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/ontstore_i2b2pm.sql

# i2b2
# ###############################################################################
sqlcmd -No -H localhost -d i2b2hive -U SA -P '<YourStrong@Passw0rd>' -i ${DIR}/i2b2hive.sql

exit 0