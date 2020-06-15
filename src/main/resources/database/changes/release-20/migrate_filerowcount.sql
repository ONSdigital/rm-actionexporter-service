UPDATE actionexporter.exportfile
SET rowcount = actionexporter.filerowcount.rowcount
FROM actionexporter.filerowcount
WHERE actionexporter.exportfile.filename = actionexporter.filerowcount.filename