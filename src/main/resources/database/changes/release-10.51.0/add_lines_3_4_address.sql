ALTER TABLE actionexporter.address ADD COLUMN address_line3 varchar(60);
ALTER TABLE actionexporter.address ADD COLUMN address_line4 varchar(60);

-- Please note that Postgres has no support for reordering the positions of columns.
-- Thus, these two columns will appear at the end of the address table.