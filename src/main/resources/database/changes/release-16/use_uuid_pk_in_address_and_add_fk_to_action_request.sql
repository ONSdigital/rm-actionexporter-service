-- Create postgres extension to allow generation of v4 UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto" SCHEMA "public";

-- Add new columns for new unique address keys and foreign keys (unique and not null to ensure it meets PK criteria)
ALTER TABLE ONLY actionexporter.address ADD COLUMN addressPK UUID DEFAULT "public".gen_random_uuid() UNIQUE NOT NULL;
ALTER TABLE ONLY actionexporter.actionrequest ADD COLUMN addressFK UUID;

-- Remove foreign key constraint from sampleunitrefFK in action request and rename accordingly
ALTER TABLE ONLY actionexporter.actionrequest DROP CONSTRAINT sampleunitrefFK_fkey;
ALTER TABLE ONLY actionexporter.actionrequest RENAME COLUMN sampleunitrefFK TO sampleunitref;

-- Remove primary key constraint from sampleunitrefPK in action address and rename accordingly
ALTER TABLE ONLY actionexporter.address DROP CONSTRAINT sampleunitrefPK_pkey;
ALTER TABLE ONLY actionexporter.address RENAME COLUMN sampleunitrefPK TO sampleunitref;

-- Add primary key constraint to addressPK
ALTER TABLE ONLY actionexporter.address ADD CONSTRAINT addressPK_pkey PRIMARY KEY (addressPK);

-- Remove now redundant unique constraint on addressPK (enforced by PK constraint)
ALTER TABLE ONLY actionexporter.address DROP CONSTRAINT IF EXISTS address_addressPK_key;

-- Add foreign key constraint to addressFK in action request
ALTER TABLE ONLY actionexporter.actionrequest ADD CONSTRAINT addressFK_fkey FOREIGN KEY (addressFK) REFERENCES actionexporter.address(addressPK);

-- Allow sampleunitref to be nullable in address
-- (will no longer be stored here but on the action request table, this column can be dropped later)
ALTER TABLE ONLY actionexporter.address ALTER COLUMN sampleunitref DROP NOT NULL;

-- Remove index on previous address FK column
DROP INDEX actionexporter.actionrequest_sampleunitrefFK_index;

-- Create index on new address FK column
CREATE INDEX actionrequest_addressFK_index on actionexporter.actionrequest(addressFK);

-- Requires separate data migration script to be run to fill in the foreign keys for existing action requests