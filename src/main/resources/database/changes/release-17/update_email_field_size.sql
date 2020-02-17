-- Change email field length to 200
ALTER TABLE ONLY actionexporter.contact ALTER COLUMN emailaddress TYPE varchar (200);