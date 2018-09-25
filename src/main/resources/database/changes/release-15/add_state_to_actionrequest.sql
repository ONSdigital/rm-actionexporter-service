ALTER TABLE actionexporter.actionrequest ADD COLUMN exportjobid UUID;

UPDATE actionexporter.actionrequest SET exportjobid = uuid_generate_v4() WHERE dateSent IS NOT NULL;

ALTER TABLE actionexporter.actionrequest DELETE COLUMN dateSent;

CREATE TABLE actionexporter.exportjob
(   id  UUID PRIMARY KEY
);

CREATE TABLE actionexporter.exportfile
(   id  UUID PRIMARY KEY,
    status filename varying(60),
    exportjobid UUID,
    datesuccessfullysent  timestamp,
    status character varying(20),
);
