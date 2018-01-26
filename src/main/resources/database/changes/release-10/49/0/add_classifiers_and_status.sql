ALTER TABLE actionexporter.actionrequest
ADD COLUMN legalbasis varchar(50),
ADD COLUMN region varchar(50),
ADD COLUMN respondentstatus varchar(50),
ADD COLUMN enrolmentstatus varchar(50),
ADD COLUMN casegroupstatus varchar(50),
ADD COLUMN surveyref varchar(50);
