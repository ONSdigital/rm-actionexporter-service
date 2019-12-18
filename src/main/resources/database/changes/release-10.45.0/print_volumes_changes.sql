ALTER TABLE actionexporter.filerowcount ADD COLUMN sendresult boolean NOT NULL;



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
       v_contents    := 'filename,rowcount,datesent,success'; -- Set header line    

       FOR r_dataline IN (SELECT * FROM actionexporter.filerowcount f WHERE NOT f.reported) LOOP
             v_contents := v_contents || chr(10) || r_dataline.filename || ',' || r_dataline.rowcount || ',' || r_dataline.datesent || ',' || r_dataline.sendresult;                                     
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
