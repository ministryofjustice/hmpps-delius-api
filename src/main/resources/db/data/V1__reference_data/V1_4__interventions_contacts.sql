INSERT INTO R_CONTACT_TYPE (CONTACT_TYPE_ID, CODE, DESCRIPTION, SHORT_DESCRIPTION, SELECTABLE,
                            NATIONAL_STANDARDS_CONTACT, ATTENDANCE_CONTACT, RECORDED_HOURS_CREDITED, SENSITIVE_CONTACT,
                            OFFENDER_LEVEL_CONTACT, CONTACT_OUTCOME_FLAG, CONTACT_LOCATION_FLAG, CONTACT_ALERT_FLAG,
                            FUTURE_SCHEDULED_CONTACTS_FLAG, APPEARS_IN_LIST_OF_CONTACTS, SMS_MESSAGE_TEXT,
                            OFFENDER_EVENT_0, LEGACY_ORDERS, CJA_ORDERS, DPA_EXCLUDE, SPG_OVERRIDE, CREATED_BY_USER_ID,
                            CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ROW_VERSION, EDITABLE,
                            SGC_FLAG, SPG_INTEREST)
SELECT (SELECT MAX(CONTACT_TYPE_ID) + 1 FROM R_CONTACT_TYPE),
       'CRS01',
       'Referred to Commissioned Rehabilitative Service',
       null,
       'N',
       'N',
       'N',
       'N',
       'N',
       'Y',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       1,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       0,
       'N',
       0,
       0
FROM DUAL
WHERE NOT EXISTS(SELECT * FROM R_CONTACT_TYPE WHERE CODE = 'CRS01');

--AL = All/Always/Forever, RA = Referrals & Assessments 
INSERT INTO R_CONTACT_TYPECONTACT_CATEGORY(CONTACT_TYPE_ID, STANDARD_REFERENCE_LIST_ID, ROW_VERSION)
SELECT (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRS01'),
       (SELECT STANDARD_REFERENCE_LIST_ID
        FROM R_STANDARD_REFERENCE_LIST
        WHERE REFERENCE_DATA_MASTER_ID =
              (SELECT REFERENCE_DATA_MASTER_ID FROM R_REFERENCE_DATA_MASTER WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
          AND CODE_VALUE = 'AL'),
       0
FROM DUAL
WHERE NOT EXISTS(SELECT *
                 FROM R_CONTACT_TYPECONTACT_CATEGORY
                 WHERE CONTACT_TYPE_ID = (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRS01')
                   AND STANDARD_REFERENCE_LIST_ID = (SELECT STANDARD_REFERENCE_LIST_ID
                                                     FROM R_STANDARD_REFERENCE_LIST
                                                     WHERE REFERENCE_DATA_MASTER_ID = (SELECT REFERENCE_DATA_MASTER_ID
                                                                                       FROM R_REFERENCE_DATA_MASTER
                                                                                       WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
                                                       AND CODE_VALUE = 'AL'));
INSERT INTO R_CONTACT_TYPECONTACT_CATEGORY(CONTACT_TYPE_ID, STANDARD_REFERENCE_LIST_ID, ROW_VERSION)
SELECT (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRS01'),
       (SELECT STANDARD_REFERENCE_LIST_ID
        FROM R_STANDARD_REFERENCE_LIST
        WHERE REFERENCE_DATA_MASTER_ID =
              (SELECT REFERENCE_DATA_MASTER_ID FROM R_REFERENCE_DATA_MASTER WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
          AND CODE_VALUE = 'RA'),
       0
FROM DUAL
WHERE NOT EXISTS(SELECT *
                 FROM R_CONTACT_TYPECONTACT_CATEGORY
                 WHERE CONTACT_TYPE_ID = (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRS01')
                   AND STANDARD_REFERENCE_LIST_ID = (SELECT STANDARD_REFERENCE_LIST_ID
                                                     FROM R_STANDARD_REFERENCE_LIST
                                                     WHERE REFERENCE_DATA_MASTER_ID = (SELECT REFERENCE_DATA_MASTER_ID
                                                                                       FROM R_REFERENCE_DATA_MASTER
                                                                                       WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
                                                       AND CODE_VALUE = 'RA'));

--Insert address for Organisation
INSERT INTO ADDRESS(ADDRESS_ID, BUILDING_NAME, CREATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_DATETIME,
                    LAST_UPDATED_USER_ID, SOFT_DELETED, PARTITION_AREA_ID)
SELECT (SELECT MAX(ADDRESS_ID) + 1 FROM ADDRESS),
       'CRS',
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       0,
       0
FROM DUAL
WHERE NOT EXISTS(SELECT * FROM ADDRESS WHERE BUILDING_NAME = 'CRS');

--Insert Organisation
INSERT INTO ORGANISATION(ORGANISATION_ID, CODE, DESCRIPTION, START_DATE, PRIVATE, ADDRESS_ID, ACTIVE_FLAG,
                         CREATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_DATETIME, LAST_UPDATED_USER_ID)
SELECT (SELECT MAX(ORGANISATION_ID) + 1 FROM ORGANISATION),
       'CRS',
       'Commissioned Rehabilitative Services Providers',
       TO_DATE('01/01/2021', 'DD/MM/YYYY'),
       1,
       (SELECT ADDRESS_ID FROM ADDRESS WHERE BUILDING_NAME = 'CRS'),
       1,
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER')
FROM DUAL
WHERE NOT EXISTS(SELECT * FROM ORGANISATION WHERE CODE = 'CRS');

--Insert Probation Area
INSERT INTO PROBATION_AREA(PROBATION_AREA_ID, CODE, DESCRIPTION, SELECTABLE, PRIVATE, ORGANISATION_ID, ADDRESS_ID,
                           START_DATE, SPG_ACTIVE_ID, CREATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_DATETIME,
                           LAST_UPDATED_USER_ID, DIVISION_ID)
SELECT (SELECT MAX(PROBATION_AREA_ID) + 1 FROM PROBATION_AREA),
       'CRS',
       'Commissioned Rehabilitative Services Provider',
       'N',
       1,
       (SELECT ORGANISATION_ID FROM ORGANISATION WHERE CODE = 'CRS'),
       (SELECT ADDRESS_ID FROM ADDRESS WHERE BUILDING_NAME = 'CRS'),
       TO_DATE('01/01/2021', 'DD/MM/YYYY'),
       (SELECT STANDARD_REFERENCE_LIST_ID
        FROM R_STANDARD_REFERENCE_LIST
        WHERE REFERENCE_DATA_MASTER_ID =
              (SELECT REFERENCE_DATA_MASTER_ID FROM R_REFERENCE_DATA_MASTER WHERE CODE_SET_NAME = 'SPG_ACTIVE')
          AND CODE_VALUE = 'N'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       (SELECT PROBATION_AREA_ID FROM PROBATION_AREA WHERE CODE = 'N41')
FROM DUAL
WHERE NOT EXISTS(SELECT * FROM PROBATION_AREA WHERE CODE = 'CRS');

--Insert Unallocated Borough
INSERT INTO BOROUGH (BOROUGH_ID, CODE, DESCRIPTION, SELECTABLE, ROW_VERSION, CREATED_BY_USER_ID, CREATED_DATETIME,
                     LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, PROBATION_AREA_ID)
SELECT (SELECT MAX(BOROUGH_ID) + 1 FROM BOROUGH),
       '' || PA.CODE || 'UAT',
       'Unallocated Cluster',
       'Y',
       1,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       PROBATION_AREA_ID
FROM PROBATION_AREA PA
WHERE PA.CODE = 'CRS'
  AND NOT EXISTS(SELECT * FROM BOROUGH B WHERE B.CODE = '' || PA.CODE || 'UAT');

--Insert Unallocated District
INSERT INTO DISTRICT (DISTRICT_ID, CODE, DESCRIPTION, SELECTABLE, BOROUGH_ID, ROW_VERSION, CREATED_BY_USER_ID,
                      CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME)
SELECT (SELECT MAX(DISTRICT_ID) + 1 FROM DISTRICT),
       '' || PA.CODE || 'UAT',
       'Unallocated LDU',
       'Y',
       (SELECT BOROUGH_ID FROM BOROUGH WHERE CODE = '' || PA.CODE || 'UAT'),
       1,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE
FROM PROBATION_AREA PA
WHERE PA.CODE = 'CRS'
  AND NOT EXISTS(SELECT * FROM DISTRICT B WHERE B.CODE = '' || PA.CODE || 'UAT');

--Insert Unallocated LDU
INSERT INTO LOCAL_DELIVERY_UNIT (LOCAL_DELIVERY_UNIT_ID, CODE, DESCRIPTION, SELECTABLE, ROW_VERSION, CREATED_BY_USER_ID,
                                 CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, PROBATION_AREA_ID)
SELECT (SELECT MAX(LOCAL_DELIVERY_UNIT_ID) + 1 FROM LOCAL_DELIVERY_UNIT),
       '' || PA.CODE || 'UAT',
       'Unallocated Team Type',
       'Y',
       1,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       PROBATION_AREA_ID
FROM PROBATION_AREA PA
WHERE PA.CODE = 'CRS'
  AND NOT EXISTS(SELECT * FROM LOCAL_DELIVERY_UNIT B WHERE B.CODE = '' || PA.CODE || 'UAT');

--Insert Unallocated Team
INSERT INTO TEAM (TEAM_ID, CODE, DESCRIPTION, DISTRICT_ID, LOCAL_DELIVERY_UNIT_ID, UNPAID_WORK_TEAM, ROW_VERSION,
                  START_DATE, CREATED_BY_USER_ID, CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME,
                  PROBATION_AREA_ID, PRIVATE)
SELECT (SELECT MAX(TEAM_ID) + 1 FROM TEAM),
       '' || PA.CODE || 'UAT',
       'Unallocated',
       (SELECT DISTRICT_ID FROM DISTRICT WHERE CODE = '' || PA.CODE || 'UAT'),
       (SELECT LOCAL_DELIVERY_UNIT_ID FROM LOCAL_DELIVERY_UNIT WHERE CODE = '' || PA.CODE || 'UAT'),
       'Y',
       1,
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       PROBATION_AREA_ID,
       1
FROM PROBATION_AREA PA
WHERE PA.CODE = 'CRS'
  AND NOT EXISTS(SELECT * FROM TEAM B WHERE B.CODE = '' || PA.CODE || 'UAT');

--Insert Unallocated Staff                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     AND NOT EXISTS (SELECT * FROM TEAM B WHERE B.CODE = ''||PA.CODE || 'UAT');
INSERT INTO STAFF (STAFF_ID, START_DATE, SURNAME, FORENAME, ROW_VERSION, OFFICER_CODE, CREATED_BY_USER_ID,
                   CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, PRIVATE, PROBATION_AREA_ID)
SELECT (SELECT MAX(STAFF_ID) + 1 FROM STAFF),
       TRUNC(SYSDATE),
       'Unallocated',
       'Unallocated',
       1,
       '' || PA.CODE || 'UATU',
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       1,
       PROBATION_AREA_ID
FROM PROBATION_AREA PA
WHERE PA.CODE = 'CRS'
  AND NOT EXISTS(SELECT * FROM STAFF B WHERE B.OFFICER_CODE = '' || PA.CODE || 'UATU');

--Insert Staff Team Link
INSERT INTO STAFF_TEAM (STAFF_ID, TEAM_ID, ROW_VERSION, CREATED_BY_USER_ID, CREATED_DATETIME, LAST_UPDATED_USER_ID,
                        LAST_UPDATED_DATETIME)
SELECT (SELECT STAFF_ID FROM STAFF WHERE OFFICER_CODE = '' || PA.CODE || 'UATU'),
       (SELECT TEAM_ID FROM TEAM WHERE CODE = '' || PA.CODE || 'UAT'),
       1,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       SYSDATE
FROM PROBATION_AREA PA
WHERE PA.CODE = 'CRS';