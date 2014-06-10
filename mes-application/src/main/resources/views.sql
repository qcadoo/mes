DROP TABLE IF EXISTS materialflowdocuments_warehousestock;

CREATE OR REPLACE VIEW materialflowdocuments_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id ;
