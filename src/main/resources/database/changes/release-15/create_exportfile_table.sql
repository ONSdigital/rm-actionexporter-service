CREATE TABLE actionexporter.exportfile
(   id  UUID PRIMARY KEY,
    filename character varying(60),
    exportjobid UUID,
    datesuccessfullysent timestamp,
    status character varying(20)
);
