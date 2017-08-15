set schema 'actionexporter';


UPDATE actionexporter.templatemapping
SET  actiontypenamePK = 'BSNOT'
   , file             = 'BSNOT'
WHERE actiontypenamePK = 'BRESEL';



UPDATE actionexporter.templatemapping
SET  actiontypenamePK = 'BSREM'
   , file             = 'BSREM'
WHERE actiontypenamePK = 'BRESERL';

