DROP TABLE IF EXISTS materialflowresources_warehousestock;

CREATE FUNCTION createWarehouseStockView () RETURNS boolean AS 'BEGIN IF EXISTS (SELECT * FROM pg_class WHERE relname=''qcadootenant_tenant'') THEN CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, SUM(quantity) AS quantity, tenantid FROM materialflowresources_resource GROUP BY tenantid, location_id, product_id ; ELSE CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id ; END IF; RETURN TRUE; END;' LANGUAGE 'plpgsql';

SELECT createWarehouseStockView();

DROP FUNCTION createWarehouseStockView ();