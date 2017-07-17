SET schema 'actionexporter';
 
ALTER TABLE actionexporter.actionrequest ADD COLUMN exerciseref character varying(20) NOT NULL;