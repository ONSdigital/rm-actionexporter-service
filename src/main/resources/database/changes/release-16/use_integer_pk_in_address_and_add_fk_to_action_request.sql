CREATE SEQUENCE addressPKseq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 999999999999
    CACHE 1;

ALTER TABLE actionexporter.address ADD COLUMN addressPK bigint;

ALTER TABLE actionexporter.actionrequest ADD COLUMN addressFK bigint;

--TODO MIGRATE DATA HERE

ALTER TABLE ONLY actionexporter.address DROP CONSTRAINT sampleunitrefPK_pkey;
ALTER TABLE ONLY actionexporter.address RENAME COLUMN sampleunitrefPK TO sampleunitref
ALTER TABLE ONLY actionexporter.address ADD CONSTRAINT addressPK_pkey PRIMARY KEY (addressPK)
