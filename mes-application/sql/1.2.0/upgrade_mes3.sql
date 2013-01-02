
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