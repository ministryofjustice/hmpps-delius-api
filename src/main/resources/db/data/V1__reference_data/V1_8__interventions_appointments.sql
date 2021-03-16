INSERT INTO R_CONTACT_TYPE (CONTACT_TYPE_ID, CODE, DESCRIPTION, SHORT_DESCRIPTION, SELECTABLE,
                            NATIONAL_STANDARDS_CONTACT, ATTENDANCE_CONTACT, RECORDED_HOURS_CREDITED, SENSITIVE_CONTACT,
                            OFFENDER_LEVEL_CONTACT, CONTACT_OUTCOME_FLAG, CONTACT_LOCATION_FLAG, CONTACT_ALERT_FLAG,
                            FUTURE_SCHEDULED_CONTACTS_FLAG, APPEARS_IN_LIST_OF_CONTACTS, SMS_MESSAGE_TEXT,
                            OFFENDER_EVENT_0, LEGACY_ORDERS, CJA_ORDERS, DPA_EXCLUDE, SPG_OVERRIDE, CREATED_BY_USER_ID,
                            CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ROW_VERSION, EDITABLE,
                            SGC_FLAG, SPG_INTEREST, RAR_ACTIVITY)
SELECT (SELECT MAX(CONTACT_TYPE_ID) + 1 FROM R_CONTACT_TYPE),
       'CRSAPT',
       'Appointment with CRS Provider (NS)',
       null,
       'N',
       'Y',
       'Y',
       'Y',
       'N',
       'Y',
       'Y',
       'Y',
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
       'Y',
       1,
       1,
       'Y'
FROM DUAL
WHERE NOT EXISTS(SELECT * FROM R_CONTACT_TYPE WHERE CODE = 'CRSAPT');

--Insert new Contact Cat - Structured Interventions (SI)
insert into r_standard_reference_list(standard_reference_list_id, code_value, code_description, selectable, created_by_user_id, created_datetime,
                                      last_updated_user_id, last_updated_datetime, reference_data_master_id, spg_interest, spg_override)
select (SELECT MAX(standard_reference_list_id) + 1 FROM r_standard_reference_list), 'SI', 'Structured Interventions', 'N', (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), sysdate,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), sysdate, (select reference_data_master_id from r_reference_data_master where code_set_name='CONTACT CATEGORY'), 1, 0
from dual WHERE NOT EXISTS
    (select * from r_standard_reference_list where reference_data_master_id in
                                                   (select reference_data_master_id from r_reference_data_master where r_reference_data_master.code_set_name='CONTACT CATEGORY') and code_value = 'SI')
;

--AL = All/Always/Forever, RA = Referrals & Assessments
INSERT INTO R_CONTACT_TYPECONTACT_CATEGORY(CONTACT_TYPE_ID, STANDARD_REFERENCE_LIST_ID, ROW_VERSION)
SELECT (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRSAPT'),
       (SELECT STANDARD_REFERENCE_LIST_ID
        FROM R_STANDARD_REFERENCE_LIST
        WHERE REFERENCE_DATA_MASTER_ID =
              (SELECT REFERENCE_DATA_MASTER_ID FROM R_REFERENCE_DATA_MASTER WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
          AND CODE_VALUE = 'AL'),
       0
FROM DUAL
WHERE NOT EXISTS(SELECT *
                 FROM R_CONTACT_TYPECONTACT_CATEGORY
                 WHERE CONTACT_TYPE_ID = (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRSAPT')
                   AND STANDARD_REFERENCE_LIST_ID = (SELECT STANDARD_REFERENCE_LIST_ID
                                                     FROM R_STANDARD_REFERENCE_LIST
                                                     WHERE REFERENCE_DATA_MASTER_ID = (SELECT REFERENCE_DATA_MASTER_ID
                                                                                       FROM R_REFERENCE_DATA_MASTER
                                                                                       WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
                                                       AND CODE_VALUE = 'AL'));

INSERT INTO R_CONTACT_TYPECONTACT_CATEGORY(CONTACT_TYPE_ID, STANDARD_REFERENCE_LIST_ID, ROW_VERSION)
SELECT (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRSAPT'),
       (SELECT STANDARD_REFERENCE_LIST_ID
        FROM R_STANDARD_REFERENCE_LIST
        WHERE REFERENCE_DATA_MASTER_ID =
              (SELECT REFERENCE_DATA_MASTER_ID FROM R_REFERENCE_DATA_MASTER WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
          AND CODE_VALUE = 'SI'),
       0
FROM DUAL
WHERE NOT EXISTS(SELECT *
                 FROM R_CONTACT_TYPECONTACT_CATEGORY
                 WHERE CONTACT_TYPE_ID = (SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE = 'CRSAPT')
                   AND STANDARD_REFERENCE_LIST_ID = (SELECT STANDARD_REFERENCE_LIST_ID
                                                     FROM R_STANDARD_REFERENCE_LIST
                                                     WHERE REFERENCE_DATA_MASTER_ID = (SELECT REFERENCE_DATA_MASTER_ID
                                                                                       FROM R_REFERENCE_DATA_MASTER
                                                                                       WHERE CODE_SET_NAME = 'CONTACT CATEGORY')
                                                       AND CODE_VALUE = 'SI'));
--Link New Contact Type to Outcomes
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AACL' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAEM' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAFC' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAHO' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAME' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAAA' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AARE' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AARC' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AASD' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'CO05' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'CO10' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATTC' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AFTC' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATSH' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATSS' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AFTA' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATFI' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'RSOF' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'RSSR' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'UAAB' and ct.code = 'CRSAPT'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

--Link new Contact Types to NSI Types
INSERT INTO R_CONTACT_TYPE_NSI_TYPE(CONTACT_TYPE_ID, NSI_TYPE_ID, ROW_VERSION)
SELECT ct.contact_type_id, nt.nsi_type_id, 1
FROM R_CONTACT_TYPE ct, R_NSI_TYPE nt
WHERE ct.code = 'CRSAPT' and nt.code = 'CRS01'
  and NOT EXISTS
    (select * from R_CONTACT_TYPE_NSI_TYPE ctn where ctn.contact_type_id = ct.contact_type_id and ctn.nsi_type_id = nt.nsi_type_id)
;
INSERT INTO R_CONTACT_TYPE_NSI_TYPE(CONTACT_TYPE_ID, NSI_TYPE_ID, ROW_VERSION)
SELECT ct.contact_type_id, nt.nsi_type_id, 1
FROM R_CONTACT_TYPE ct, R_NSI_TYPE nt
WHERE ct.code = 'CRSAPT' and nt.code = 'CRS02'
  and NOT EXISTS
    (select * from R_CONTACT_TYPE_NSI_TYPE ctn where ctn.contact_type_id = ct.contact_type_id and ctn.nsi_type_id = nt.nsi_type_id)
;
INSERT INTO R_CONTACT_TYPE_NSI_TYPE(CONTACT_TYPE_ID, NSI_TYPE_ID, ROW_VERSION)
SELECT ct.contact_type_id, nt.nsi_type_id, 1
FROM R_CONTACT_TYPE ct, R_NSI_TYPE nt
WHERE ct.code = 'CRSAPT' and nt.code = 'CRS03'
  and NOT EXISTS
    (select * from R_CONTACT_TYPE_NSI_TYPE ctn where ctn.contact_type_id = ct.contact_type_id and ctn.nsi_type_id = nt.nsi_type_id)
;
INSERT INTO R_CONTACT_TYPE_NSI_TYPE(CONTACT_TYPE_ID, NSI_TYPE_ID, ROW_VERSION)
SELECT ct.contact_type_id, nt.nsi_type_id, 1
FROM R_CONTACT_TYPE ct, R_NSI_TYPE nt
WHERE ct.code = 'CRSAPT' and nt.code = 'CRS04'
  and NOT EXISTS
    (select * from R_CONTACT_TYPE_NSI_TYPE ctn where ctn.contact_type_id = ct.contact_type_id and ctn.nsi_type_id = nt.nsi_type_id)
;
INSERT INTO R_CONTACT_TYPE_NSI_TYPE(CONTACT_TYPE_ID, NSI_TYPE_ID, ROW_VERSION)
SELECT ct.contact_type_id, nt.nsi_type_id, 1
FROM R_CONTACT_TYPE ct, R_NSI_TYPE nt
WHERE ct.code = 'CRSAPT' and nt.code = 'CRS05'
  and NOT EXISTS
    (select * from R_CONTACT_TYPE_NSI_TYPE ctn where ctn.contact_type_id = ct.contact_type_id and ctn.nsi_type_id = nt.nsi_type_id)
;
INSERT INTO R_CONTACT_TYPE_NSI_TYPE(CONTACT_TYPE_ID, NSI_TYPE_ID, ROW_VERSION)
SELECT ct.contact_type_id, nt.nsi_type_id, 1
FROM R_CONTACT_TYPE ct, R_NSI_TYPE nt
WHERE ct.code = 'CRSAPT' and nt.code = 'CRS06'
  and NOT EXISTS
    (select * from R_CONTACT_TYPE_NSI_TYPE ctn where ctn.contact_type_id = ct.contact_type_id and ctn.nsi_type_id = nt.nsi_type_id)
;
INSERT INTO R_CONTACT_TYPE_NSI_TYPE(CONTACT_TYPE_ID, NSI_TYPE_ID, ROW_VERSION)
SELECT ct.contact_type_id, nt.nsi_type_id, 1
FROM R_CONTACT_TYPE ct, R_NSI_TYPE nt
WHERE ct.code = 'CRSAPT' and nt.code = 'CRS07'
  and NOT EXISTS
    (select * from R_CONTACT_TYPE_NSI_TYPE ctn where ctn.contact_type_id = ct.contact_type_id and ctn.nsi_type_id = nt.nsi_type_id)
;

