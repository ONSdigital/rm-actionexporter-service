CREATE SEQUENCE actionexporter.addressPKseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

ALTER TABLE actionexporter.address ADD COLUMN addressPK bigint DEFAULT nextval('actionexporter.addressPKseq') NOT NULL;

ALTER TABLE actionexporter.actionrequest ADD COLUMN addressFK bigint;

UPDATE actionexporter.actionrequest AS ar
SET addressfk = a.addresspk
FROM actionexporter.address AS a
WHERE ar.sampleunitreffk = a.sampleunitrefpk;

ALTER TABLE ONLY actionexporter.actionrequest DROP CONSTRAINT sampleunitrefFK_fkey;
ALTER TABLE ONLY actionexporter.actionrequest RENAME COLUMN sampleunitrefFK TO sampleunitref;
ALTER TABLE ONLY actionexporter.address DROP CONSTRAINT sampleunitrefPK_pkey;
ALTER TABLE ONLY actionexporter.address ADD CONSTRAINT addressPK_pkey PRIMARY KEY (addressPK);
ALTER TABLE ONLY actionexporter.actionrequest ADD CONSTRAINT addressFK_fkey FOREIGN KEY (addressFK) REFERENCES actionexporter.address(addressPK);
ALTER TABLE ONLY actionexporter.address DROP COLUMN sampleunitrefPK;
