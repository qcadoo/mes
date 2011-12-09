CREATE OR REPLACE FUNCTION update_view() RETURNS INTEGER AS 
'
	DECLARE
		tenant RECORD;  
	BEGIN  
		FOR tenant IN SELECT * FROM qcadootenant_tenant LOOP  
			INSERT INTO qcadooview_view (id, pluginidentifier, name, view, tenantid) 
			VALUES (
				nextval(''hibernate_sequence''), 
				''basic'', 
				''companiesList'', 
				''companiesList'', 
				tenant."id"
			);

			INSERT INTO qcadooview_item (id, pluginidentifier, name, active, category_id, view_id, succession, tenantid) 
			VALUES (
				nextval(''hibernate_sequence''), 
				''basic'', 
				''companies'', 
				true,  
				(SELECT id FROM qcadooview_category WHERE tenantid = tenant."id" AND name=''basic'' LIMIT 1), 
				(SELECT id FROM qcadooview_view WHERE tenantid = tenant."id" AND name=''companiesList'' LIMIT 1), 
				(SELECT COUNT(*) FROM qcadooview_item WHERE tenantid = tenant."id" AND category_id = (SELECT id FROM qcadooview_category WHERE tenantid = tenant."id" AND name=''basic'')) + 1,
				tenant."id"
			); 
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_view();