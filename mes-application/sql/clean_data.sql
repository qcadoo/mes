CREATE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION disable_triggers() RETURNS INTEGER AS 
'
	DECLARE
		tab_data RECORD;  
	BEGIN  
		FOR tab_data IN SELECT * FROM information_schema.tables WHERE table_schema = ''public'' LOOP 
			EXECUTE ''ALTER TABLE '' || tab_data.table_name || '' DISABLE TRIGGER ALL'';
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION enable_triggers() RETURNS INTEGER AS 
'
	DECLARE
		tab_data RECORD;  
	BEGIN  
		FOR tab_data IN SELECT * FROM information_schema.tables WHERE table_schema = ''public'' LOOP 
			EXECUTE ''ALTER TABLE '' || tab_data.table_name || '' ENABLE TRIGGER ALL'';
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION clean_data() RETURNS INTEGER AS 
'
	DECLARE
		tab_data RECORD;  
		material_id INTEGER;
	BEGIN  
		FOR tab_data IN SELECT * FROM information_schema.tables WHERE table_schema = ''public'' LOOP 
			IF tab_data.table_name NOT IN  (''qcadooplugin_plugin'', ''qcadootenant_tenant'', ''jointable_materialrequirement_order'') THEN
				IF tab_data.table_name IN (''materialrequirements_materialrequirement'') THEN
					FOR material_id IN SELECT id FROM materialrequirements_materialrequirement WHERE tenantid = 0 LOOP
						EXECUTE ''DELETE FROM jointable_materialrequirement_order 
							 WHERE materialrequirement_id = '' || material_id ;	
					END LOOP;	 
				END IF;
				EXECUTE ''DELETE FROM '' || tab_data.table_name || '' WHERE tenantid = 0'';
			END IF;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';



BEGIN
SELECT * FROM disable_triggers();
SELECT * FROM clean_data();
SELECT * FROM enable_triggers();
END