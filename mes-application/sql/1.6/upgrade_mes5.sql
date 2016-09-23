-- technologies_technology
-- by kasi 05.09.2016

ALTER TABLE technologies_technology ADD COLUMN standardperformancetechnology numeric(12,5);
ALTER TABLE technologies_technology ADD COLUMN template boolean;
ALTER TABLE technologies_technology ALTER COLUMN template SET DEFAULT false;
UPDATE technologies_technology SET template=false;

-- end

-- last touched 09.09.2016 by kasi

CREATE OR REPLACE VIEW materialflowresources_storagelocationdto_internal AS
    select location.number as locationNumber, storageLocation.number as storageLocationNumber,
    	COALESCE(product.number, storageProduct.number) as productNumber, COALESCE(product.name, storageProduct.name) as productName, resourceCode.code as additionalCode,
    	COALESCE(SUM(resource.quantity), 0::numeric) as resourceQuantity, COALESCE(product.unit, storageProduct.unit) as productUnit,
    	COALESCE(SUM(resource.quantityinadditionalunit), 0::numeric) as quantityInAdditionalUnit,
    	COALESCE(product.additionalunit, product.unit, storageProduct.additionalunit, storageProduct.unit) as productAdditionalUnit
    from materialflowresources_storagelocation storageLocation
   	join materialflow_location location on storageLocation.location_id = location.id
   	left join materialflowresources_resource resource on resource.storagelocation_id = storageLocation.id
   	left join basic_product product on product.id = resource.product_id
   	left join basic_product storageProduct on storageProduct.id = storageLocation.product_id
   	left join basic_additionalcode resourceCode on resourceCode.id = resource.additionalcode_id
    where storageLocation.active = true
   	group by locationNumber, storageLocationNumber, productNumber, productName, additionalCode, productUnit, productAdditionalUnit;

CREATE OR REPLACE VIEW materialflowresources_storagelocationdto AS
	select row_number() OVER () AS id, internal.*
	from materialflowresources_storagelocationdto_internal internal;

-- end

-- master order labels
-- last touched 06.09.2016 by kama

ALTER TABLE basic_parameter ADD COLUMN additionalimage character varying(255);

-- end

-- table: qcadoosecurity_role, qcadoosecurity_group
-- last touched 16.09.2016 by lupo

UPDATE qcadoosecurity_role SET description = null;
UPDATE qcadoosecurity_group SET description = null;

-- end

-- begin ANEKS-4

CREATE TABLE jointable_productionline_shift
(
  productionline_id bigint NOT NULL,
  shift_id bigint NOT NULL,
  CONSTRAINT jointable_productionline_shift_pkey PRIMARY KEY (productionline_id, shift_id),
  CONSTRAINT shift_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT productionline_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE
);

CREATE OR REPLACE FUNCTION import_productionline_shift() RETURNS VOID AS $$ DECLARE rowProductionLine record; rowShift record;  BEGIN FOR rowShift IN select * from basic_shift  LOOP FOR rowProductionLine IN select * from productionlines_productionline  LOOP EXECUTE 'INSERT INTO jointable_productionline_shift (productionline_id, shift_id) VALUES ('||rowProductionLine.id||','||rowShift.id||')'; END  LOOP; END LOOP;END;$$ LANGUAGE 'plpgsql';

SELECT * FROM import_productionline_shift();

DROP FUNCTION import_productionline_shift();

-- end


-- table: orders_order
-- last touched 11.09.2016 by lupo

ALTER TABLE orders_order ADD COLUMN ordercategory character varying(255);

-- end

-- view: orders_orderplanninglistdto
-- last touched 11.09.2016 by lupo

DROP VIEW orders_orderplanninglistdto;

CREATE OR REPLACE VIEW orders_orderplanninglistdto AS
	SELECT ordersorder.id,
		ordersorder.active,
		ordersorder.number,
		ordersorder.name,
		ordersorder.datefrom,
		ordersorder.dateto,
		ordersorder.startdate,
		ordersorder.finishdate,
		ordersorder.state,
		ordersorder.externalnumber,
		ordersorder.externalsynchronized,
		ordersorder.issubcontracted,
		ordersorder.plannedquantity,
		ordersorder.workplandelivered,
		ordersorder.ordercategory,
		product.number AS productnumber,
		technology.number AS technologynumber,
		product.unit AS unit,
		productionline.number AS productionlinenumber,
		masterorder.number AS masterordernumber,
		division.name AS divisionname
	FROM orders_order ordersorder
	JOIN basic_product product
		ON product.id = ordersorder.product_id
	LEFT JOIN technologies_technology technology
		ON technology.id = ordersorder.technology_id
	LEFT JOIN productionlines_productionline productionline
		ON productionline.id = ordersorder.productionline_id
	LEFT JOIN masterorders_masterorder masterorder
		ON masterorder.id = ordersorder.masterorder_id
	LEFT JOIN basic_division division
		ON division.id = technology.division_id;

-- end

-- last touched 22.09.2016 by bafl
ALTER TABLE basic_company ADD COLUMN logoImage character varying(255);
--end

-- table: orders_order
-- last touched 22.09.2016 by lupo

CREATE TABLE orders_ordercategorycolor
(
  id bigint NOT NULL,
  parameter_id bigint,
  ordercategory character varying(255),
  color character varying(255),
  CONSTRAINT orders_ordercategorycolor_pkey PRIMARY KEY (id),
  CONSTRAINT ordercategorycolor_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES public.basic_parameter (id) DEFERRABLE
);

-- end

-- address in orders
-- last touched 19.09.2016 by kama

ALTER TABLE orders_order ADD COLUMN address_id bigint;
ALTER TABLE orders_order
  ADD CONSTRAINT order_address_fkey FOREIGN KEY (address_id)
      REFERENCES basic_address (id) DEFERRABLE;

ALTER TABLE masterorders_masterorder ADD COLUMN address_id bigint;
ALTER TABLE masterorders_masterorder
  ADD CONSTRAINT masterorder_address_fkey FOREIGN KEY (address_id)
      REFERENCES basic_address (id) DEFERRABLE;

-- end