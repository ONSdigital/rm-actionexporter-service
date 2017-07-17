
-- Sequence: actionexporter.reportPKseq
-- DROP SEQUENCE actionexporter.reportPKseq;

CREATE SEQUENCE actionexporter.reportPKseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;


-- Table: actionexporter.reporttype
-- DROP TABLE actionexporter.reporttype;

CREATE TABLE actionexporter.reporttype
(   reporttypePK  character varying (20),
    displayorder  integer,
    displayname   character varying(40),
    CONSTRAINT reporttype_pkey PRIMARY KEY (reporttypePK)
);


-- Table: actionexporter.report
-- DROP TABLE actionexporter.report;

CREATE TABLE actionexporter.report
(
    id             uuid NOT NULL,
    reportPK       bigint NOT NULL,
    reporttypeFK   character varying (20),
    contents       text ,
    createddatetime timestamp with time zone,
    CONSTRAINT report_pkey PRIMARY KEY (reportpk),
    CONSTRAINT report_uuid_key UNIQUE (id),
    CONSTRAINT reporttypefk_fkey FOREIGN KEY (reporttypefk)
    REFERENCES actionexporter.reporttype (reporttypepk) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


-- Sequence: actionexporter.messageseq
-- DROP SEQUENCE actionexporter.messageseq;

CREATE SEQUENCE actionexporter.messageseq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999
  START 1
  CACHE 1;


-- Table: actionexporter.messagelog
-- DROP TABLE actionexporter.messagelog;

CREATE TABLE actionexporter.messagelog
(
  messagePK bigint NOT NULL DEFAULT nextval('actionexporter.messageseq'::regclass),
  messagetext character varying,
  jobid numeric,
  messagelevel character varying,
  functionname character varying,
  createddatetime timestamp with time zone,
  CONSTRAINT messagePK_pkey PRIMARY KEY (messagePK)
)
WITH (
  OIDS=FALSE
);


-- Insert print volume report into report tables 
INSERT INTO actionexporter.reporttype(reporttypepk,displayorder,displayname) VALUES('PRINT_VOLUMES',10,'Print Volumes');

-- Function: actionexporter.logmessage(text, numeric, text, text)

-- DROP FUNCTION actionexporter.logmessage(text, numeric, text, text);

CREATE OR REPLACE FUNCTION actionexporter.logmessage(p_messagetext text DEFAULT NULL::text, p_jobid numeric DEFAULT NULL::numeric, p_messagelevel text DEFAULT NULL::text, p_functionname text DEFAULT NULL::text)
  RETURNS boolean AS
$BODY$
DECLARE
v_text TEXT ;
v_function TEXT;
BEGIN

INSERT INTO actionexporter.messagelog (messagetext, jobid, messagelevel, functionname, createddatetime )
VALUES (p_messagetext, p_jobid, p_messagelevel, p_functionname, current_timestamp);

  RETURN TRUE;
EXCEPTION
WHEN OTHERS THEN
RETURN FALSE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;



-- Function: actionexporter.generate_print_volumes_mi()
-- DROP FUNCTION actionexporter.generate_print_volumes_mi();

CREATE OR REPLACE FUNCTION actionexporter.generate_print_volumes_mi()
  RETURNS boolean AS
$BODY$
DECLARE

v_contents      text;
r_dataline      record;
v_rows          integer;

BEGIN
    
    PERFORM actionexporter.logmessage(p_messagetext := 'GENERATING PRINT VOLUMES MI REPORT'
                              ,p_jobid := 0
                              ,p_messagelevel := 'INFO'
                              ,p_functionname := 'actionexporter.generate_print_volumes_mi');  
    
       v_rows := 0;
       v_contents    := '';
       v_contents    := 'filename,rowcount,datesent'; -- Set header line    

       FOR r_dataline IN (SELECT * FROM actionexporter.filerowcount f WHERE NOT f.reported) LOOP
             v_contents := v_contents || chr(10) || r_dataline.filename || ',' || r_dataline.rowcount || ',' || r_dataline.datesent;                                     
             v_rows := v_rows+1;  
             UPDATE actionexporter.filerowcount   
             SET reported = TRUE;
       END LOOP;       

       IF v_rows > 0 THEN  
          -- Insert the data into the report table
          INSERT INTO actionexporter.report(id, reportPK,reporttypeFK,contents, createddatetime) VALUES(gen_random_uuid(), nextval('actionexporter.reportPKseq'), 'PRINT_VOLUMES', v_contents, CURRENT_TIMESTAMP); 
       END IF;

       PERFORM actionexporter.logmessage(p_messagetext := 'GENERATING PRINT VOLUMES MI REPORT COMPLETED ROWS WRIITEN = ' || v_rows
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'actionexporter.generate_print_volumes_mi'); 
      
    
       PERFORM actionexporter.logmessage(p_messagetext := 'PRINT VOLUMES MI REPORT GENERATED'
                                        ,p_jobid := 0
                                        ,p_messagelevel := 'INFO'
                                        ,p_functionname := 'actionexporter.generate_print_volumes_mi'); 
  RETURN TRUE;

  EXCEPTION
  WHEN OTHERS THEN   
     PERFORM actionexporter.logmessage(p_messagetext := 'GENERATE PRINT VOLUMES MI REPORT EXCEPTION TRIGGERED SQLERRM: ' || 

SQLERRM || ' SQLSTATE : ' || SQLSTATE
                               ,p_jobid := 0
                               ,p_messagelevel := 'FATAL'
                               ,p_functionname := 'actionexporter.generate_print_volumes_mi');
                               
  RETURN FALSE;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  COST 100;
