--remove randomly generated uuids
ALTER TABLE actionexporter.address
    ALTER COLUMN addresspk drop DEFAULT;