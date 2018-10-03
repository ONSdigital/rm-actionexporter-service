CREATE TABLE actionexporter.exportjob
(   id  UUID PRIMARY KEY
);

CREATE TABLE actionexporter.exportfile
(   id  UUID PRIMARY KEY,
    filename character varying(60),
    exportjobid UUID REFERENCES actionexporter.exportjob(id),
    datesuccessfullysent timestamp,
    status character varying(20)
);

ALTER TABLE actionexporter.actionrequest ADD COLUMN exportjobid UUID;

ALTER TABLE ONLY actionexporter.actionrequest
ADD CONSTRAINT actionrequestexportjobid_fkey
FOREIGN KEY (exportjobid) REFERENCES actionexporter.exportjob(id);

--This adds a random UUID so that these actionrequests aren't resent
INSERT INTO actionexporter.exportjob VALUES ('9d1c1077-40bb-4145-b18a-80e7246cc27c');

UPDATE actionexporter.actionrequest SET exportjobid = '9d1c1077-40bb-4145-b18a-80e7246cc27c'
WHERE datesent IS NOT NULL;

--We don't need this anymore, because it can be figured out through the relationship:
-- actionrequest -> exportjob -> exportfile
ALTER TABLE actionexporter.actionrequest DROP COLUMN datesent;