export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv

# Start Oracle
sqlplus / as sysdba <<EOF
STARTUP;
EXIT;
EOF

# Start listener
lsnrctl start
echo -n Waiting for listener to start
until lsnrctl status | grep -q 'Service "xepdb1" has 1 instance(s).'; do echo -n .; sleep 1; done