#!/usr/bin/env bash
set -ex

export ORACLE_SID=XE
ORAENV_ASK=NO . oraenv

export ND_INSTALL_ROOT=/scripts/delius/install
cd $ND_INSTALL_ROOT || exit 1

# Update the PDM install scripts to use the XEPDB1 service name
# (this is quite brittle but the scripts rarely change, so it's okay for now)
sed -i 's/ ""/ "XEPDB1"/' install_operational_scripts.sh
sed -i 's/$DBAPPUSER\/$DBAPPPASS/$DBAPPUSER\/$DBAPPPASS@XEPDB1/' install_operational_scripts.sh
sed -i 's/echo $DBAPPPASS | sqlplus $DBAPPUSER/echo exit | sqlplus $DBAPPUSER\/$DBAPPPASS@XEPDB1/' install_operational_scripts.sh
sed -i 's/echo $DBSYSPASS | sqlplus $DBSYSUSER/echo exit | sqlplus $DBSYSUSER\/$DBSYSPASS@XEPDB1/' install_operational_scripts.sh
sed -i 's/conn SYS\/\&\&P_SYS_PASSWORD /conn SYS\/\&\&P_SYS_PASSWORD@XEPDB1 /' ../Operational\ Database\ Scripts/001_create_application_schema.sql
sed -i 's/conn \&\&P_TARGET_SCHEMA\/\&\&P_APP_SCHEMA_PASSWORD/conn \&\&P_TARGET_SCHEMA\/\&\&P_APP_SCHEMA_PASSWORD@XEPDB1 /' ../Operational\ Database\ Scripts/001_create_application_schema.sql
sed -i '1s/^/ALTER SESSION SET CONTAINER=XEPDB1;\n/' ../Operational\ Database\ Scripts/002_grants.sql

# Run the 008 script twice, it fails the first time
sed -i 's/.*008_SPG_Setup.sql.*/&\n&/' install_operational_scripts.sh

# Run uplift scripts
bash ./install_operational_scripts.sh