
-- Table: productioncounting_productionrecord
-- changed: 28.11.2012
ALTER TABLE productioncounting_productionrecord ADD COLUMN createdate timestamp without time zone;
ALTER TABLE productioncounting_productionrecord ADD COLUMN updatedate timestamp without time zone;
ALTER TABLE productioncounting_productionrecord ADD COLUMN createuser character varying(255);
ALTER TABLE productioncounting_productionrecord ADD COLUMN updateuser character varying(255);

-- end

-- Table: orders_order
-- changed: 28.11.2012
Alter table orders_order  DROP COLUMN  ordergroupname; 

-- end



CREATE OR REPLACE FUNCTION update_createdate() RETURNS INTEGER AS 
'
DECLARE
		productionrecord RECORD;
		tenant RECORD;

	BEGIN 
	FOR tenant IN SELECT * FROM qcadootenant_tenant LOOP  	
		FOR productionrecord IN SELECT * FROM productioncounting_productionrecord WHERE tenantid=tenant."id" LOOP  	
			UPDATE  productioncounting_productionrecord  SET createdate =
				(SELECT dateandtime from productioncounting_productionrecordstatechange  WHERE productionrecord_id=productionrecord."id" 
					and tenantid=tenant."id" ORDER BY dateandtime ASC LIMIT 1) 
					WHERE id=productionrecord."id" 	and tenantid=tenant."id" ;

		END  LOOP;
	END  LOOP;
RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_createdate();

DROP FUNCTION update_createdate();
