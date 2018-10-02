--This adds random UUIDs so that these actionrequests aren't resent
UPDATE actionexporter.actionrequest SET exportjobid = '9d1c1077-40bb-4145-b18a-80e7246cc27c' WHERE datesent IS NOT NULL;

--We don't need this anymore, because it can be figured out through the relationship:
-- actionrequest -> exportjob -> exportfile
ALTER TABLE actionexporter.actionrequest DROP COLUMN datesent;