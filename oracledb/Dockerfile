FROM oracle/database:18.4.0-xe AS base

ARG ORACLE_PWD=NDAmanager1
ARG DELIUS_PWD=NDelius1
ARG ORACLE_CHARACTERSET=AL32UTF8
ENV ORACLE_PWD=$ORACLE_PWD \
    DELIUS_PWD=$DELIUS_PWD \
    ORACLE_CHARACTERSET=$ORACLE_CHARACTERSET \
    XE_LOG_FILE=/opt/oracle/cfgtoollogs/dbca/XE/XE.log

FROM base AS import

# Initialise database
COPY scripts/shutdown.sh scripts/startup.sh /scripts/
RUN exec $ORACLE_BASE/$RUN_FILE & \
    until [[ -f $XE_LOG_FILE ]] && [[ $(grep 'Database creation complete' $XE_LOG_FILE) ]]; do sleep 1; done && \
    chmod +x /scripts/*.sh && \
    su - oracle -c '/scripts/shutdown.sh;'

# Create tablespaces and directories, then import the datapump file
COPY scripts/setup.sql scripts/post-import.sql /scripts/
COPY scripts/zoneinfo $ORACLE_HOME/oracore/zoneinfo
COPY import.dmp /datapump/import.dmp
RUN chmod +x /scripts/*.sh && chown -R oracle:oinstall $ORACLE_HOME/oracore /datapump && \
    su - oracle -c "/scripts/startup.sh" && \
    su - oracle -c "export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv; sqlplus sys/${ORACLE_PWD}@xepdb1 as sysdba @/scripts/setup.sql" && \
    su - oracle -c "export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv; impdp \'sys/${ORACLE_PWD}@xepdb1 as sysdba\' full=Y directory=IMPORT_DIR dumpfile=import.dmp logfile=import.log"; \
    su - oracle -c "export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv; sqlplus system/${ORACLE_PWD}@xepdb1 @/scripts/post-import.sql ${DELIUS_PWD}" && \
    su - oracle -c "/scripts/shutdown.sh" && \
    rm /datapump/import.dmp

# Uplift the PDM version and install the latest Delius PLSQL code
COPY scripts /scripts/
RUN exec $ORACLE_BASE/$RUN_FILE & \
    until lsnrctl status | grep -q 'Service "xepdb1" has 1 instance(s).'; do echo -n .; sleep 1; done && \
    chmod +x /scripts/*.sh && chmod +x /scripts/delius/install/*.sh && chown -R oracle:oinstall /scripts && \
    su - oracle -c "/scripts/uplift.sh && /scripts/shutdown.sh"

# Flatten the image to reduce final size
FROM base
COPY --from=import $ORACLE_HOME/oracore $ORACLE_HOME/oracore
COPY --from=import /opt/oracle/oradata /opt/oracle/oradata
COPY --from=import /scripts /scripts

EXPOSE 1521
EXPOSE 5500
CMD exec $ORACLE_BASE/$RUN_FILE
