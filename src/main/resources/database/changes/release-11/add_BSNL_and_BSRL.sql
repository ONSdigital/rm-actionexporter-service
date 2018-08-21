INSERT INTO actionexporter.templatemapping(
actiontypenamepk, templatenamefk, filenameprefix, datemodified)
VALUES ('BSNL', 'initialPrint', 'BSNOT',now())
ON CONFLICT (actiontypenamepk) DO NOTHING;

INSERT INTO actionexporter.templatemapping(
actiontypenamepk, templatenamefk, filenameprefix, datemodified)
VALUES ('BSRL', 'initialPrint', 'BSREM', now())
ON CONFLICT (actiontypenamepk) DO NOTHING;
