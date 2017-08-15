-- ACTIONEXPORTER SERVICE

-- templatemapping table

-- Index: actionexporter.templatemapping_templatenamefk_index
-- DROP INDEX actionexporter.templatemapping_templatenamefk_index;

CREATE INDEX templatemapping_templatenamefk_index ON actionexporter.templatemapping USING btree (templatenamefk);
