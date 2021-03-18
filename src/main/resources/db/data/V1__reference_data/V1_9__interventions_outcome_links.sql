
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AACL' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAEM' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAFC' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAHO' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAME' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AAAA' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AARE' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AARC' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AASD' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'CO05' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'CO10' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATTC' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AFTC' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATSH' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATSS' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'AFTA' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'ATFI' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'RSOF' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'RSSR' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;

INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
select ct.CONTACT_TYPE_ID, cot.CONTACT_OUTCOME_TYPE_ID,
       0, null
FROM r_contact_outcome_type cot, R_CONTACT_TYPE ct
where cot.code = 'UAAB' and ct.code = 'CRS01'
  and not exists (select cotc.* from R_CONTACT_TYPE_OUTCOME cotc
                  where cotc.CONTACT_OUTCOME_TYPE_ID = cot.CONTACT_OUTCOME_TYPE_ID and cotc.CONTACT_TYPE_ID = ct.CONTACT_TYPE_ID)
;
