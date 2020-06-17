ALTER TABLE ONLY actionexporter.actionrequest
ADD CONSTRAINT actionrequestexportfile
FOREIGN KEY (exportjobid) REFERENCES actionexporter.exportfile(exportjobid);