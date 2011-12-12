-- Table: basic_company

-- changed 12.12.2011

ALTER TABLE basic_company
        RENAME COLUMN companyfullname TO name;
ALTER TABLE basic_company      
        RENAME COLUMN adresswww TO website;
        
ALTER TABLE basic_company
        ALTER COLUMN tax TYPE character varying(255),
        ALTER COLUMN zipcode TYPE character varying(6),
        ALTER COLUMN state TYPE character varying(255),
        ALTER COLUMN country TYPE character varying(255),
        ALTER COLUMN email TYPE character varying(255),
        ALTER COLUMN phone TYPE character varying(255);
		ADD COLUMN number character varying(255),
		ADD COLUMN owner boolean DEFAULT false;


-- end
        
-- Table: technologies_technology

ALTER TABLE technologies_technology 
	ALTER COLUMN state SET DEFAULT '01draft'::character varying;

BEGIN;
	UPDATE technologies_technology SET state = '01draft' WHERE state = 'draft';
	UPDATE technologies_technology SET state = '02accepted' WHERE state = 'accepted';
	UPDATE technologies_technology SET state = '03declined' WHERE state = 'declined';
	UPDATE technologies_technology SET state = '04outdated' WHERE state = 'outdated';
COMMIT;

-- end

ALTER TABLE basic_currency DROP COLUMN isactive;

UPDATE basic_company SET owner = true;

UPDATE qcadooview_view SET name='company' WHERE name='companyDetails';

UPDATE qcadooview_item SET name='company' WHERE name='companyDetails';

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