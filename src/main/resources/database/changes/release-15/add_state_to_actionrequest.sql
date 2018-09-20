ALTER TABLE actionexporter.actionrequest ADD COLUMN sendstate varchar(20);

UPDATE actionexporter.actionrequest SET sendstate = 'ABOUT_TO_SEND' WHERE dateSent IS NULL;

UPDATE actionexporter.actionrequest SET sendstate = 'SENT' WHERE dateSent IS NOT NULL;

ALTER TABLE actionexporter.actionrequest ALTER COLUMN sendstate SET NOT NULL;
