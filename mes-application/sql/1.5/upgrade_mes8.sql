
-- worker to change in order
-- last touched 27.01.2016 by kama

ALTER TABLE orders_order ADD COLUMN workertochange character varying(255);

-- end

-- #QCADOO-432
-- add number field
alter table materialflowresources_resource  add column number character varying(255);

-- add functions and trigger

CREATE OR REPLACE FUNCTION generate_and_set_resource_number(_time timestamp)
  RETURNS text AS
$$
DECLARE
	_pattern text;
	_year numeric;
	_sequence_name text;
	_sequence_value numeric;
	_tmp text;
	_seq text;
	_number text;
BEGIN
	_pattern := '#year/#seq';
	_year := extract(year from _time);

	_sequence_name := 'materialflowresources_resource_number_' || _year;
	
	SELECT sequence_name into _tmp FROM information_schema.sequences where sequence_schema = 'public' 
		and sequence_name = _sequence_name;
	if _tmp is null then
		execute 'CREATE SEQUENCE ' || _sequence_name || ';';
	end if;

	select nextval(_sequence_name) into _sequence_value;

	_seq := to_char(_sequence_value, 'fm00000');
	if _seq like '%#%' then
		_seq := _sequence_value;
	end if;
	
	_number := _pattern;
	_number := replace(_number, '#year', _year::text);
	_number := replace(_number, '#seq', _seq);
	
	RETURN _number;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION generate_and_set_resource_number_trigger()
  RETURNS trigger AS
$$
BEGIN
	NEW.number := generate_and_set_resource_number(NEW.time);

	return NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER materialflowresources_resource_trigger_number
  BEFORE INSERT
  ON materialflowresources_resource
  FOR EACH ROW
  EXECUTE PROCEDURE generate_and_set_resource_number_trigger();

-- migrate old data
CREATE OR REPLACE FUNCTION migrate_resource_numbers()
  RETURNS void AS
$$
DECLARE
	row record;
BEGIN
    FOR row IN (select * from materialflowresources_resource resource order by time asc, id asc)
    LOOP
	update materialflowresources_resource set number = generate_and_set_resource_number(row.time) where id = row.id;    
            
    END LOOP;
END;
$$ LANGUAGE 'plpgsql';

select migrate_resource_numbers();
drop function migrate_resource_numbers();


alter table materialflowresources_resource alter column number set not null;
alter table materialflowresources_resource alter column time set not null;
alter table materialflowresources_resource add unique(number);
--end #QCADOO-432


-- productioncounting_productiontrackingdto
-- last touched 09.02.2016 by lupo

CREATE SEQUENCE productioncounting_productiontrackingdto_id_seq;

CREATE OR REPLACE VIEW productioncounting_productiontrackingdto AS
	SELECT
		productiontracking.id AS id,
		productiontracking.number AS number,
		productiontracking.state AS state,
		productiontracking.createdate AS createdate,
		productiontracking.lasttracking AS lasttracking,
		productiontracking.timerangefrom AS timerangefrom,
		productiontracking.timerangeto AS timerangeto,
		productiontracking.active AS active,
		ordersorder.id::integer AS order_id,
		ordersorder.number AS ordernumber,
		ordersorder.state AS orderstate,
		technologyoperationcomponent.id::integer AS technologyoperationcomponent_id,
		(CASE WHEN technologyoperationcomponent IS NULL THEN '' ELSE (technologyoperationcomponent.nodenumber::text || ' '::text) || operation.name::text END) AS technologyoperationcomponentnumber,
		operation.id::integer AS operation_id,
		shift.id::integer AS shift_id,
		shift.name AS shiftname,
		staff.id::integer AS staff_id,
		staff.name || ' ' || staff.surname AS staffname,
		division.id::integer AS division_id,
		division.number AS divisionnumber,
		subcontractor.id::integer AS subcontractor_id,
		subcontractor.name AS subcontractorname
	FROM productioncounting_productiontracking productiontracking
	LEFT JOIN orders_order ordersorder
		ON ordersorder.id = productiontracking.order_id
	LEFT JOIN technologies_technologyoperationcomponent technologyoperationcomponent
		ON technologyoperationcomponent.id = productiontracking.technologyoperationcomponent_id
	LEFT JOIN technologies_operation operation
		ON operation.id = technologyoperationcomponent.operation_id
	LEFT JOIN basic_shift shift
		ON shift.id = productiontracking.shift_id
	LEFT JOIN basic_staff staff
		ON staff.id = productiontracking.staff_id
	LEFT JOIN basic_division division
		ON division.id = productiontracking.division_id
	LEFT JOIN basic_company subcontractor ON subcontractor.id = productiontracking.subcontractor_id;

-- end


-- productioncounting_trackingoperationproductincomponentdto
-- last touched 14.02.2016 by lupo

CREATE SEQUENCE productioncounting_trackingoperationproductincomponentdto_id_seq;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductincomponentdto AS
	SELECT
		trackingoperationproductincomponent.id AS id,
		productiontracking.id::integer AS productiontracking_id,
		product.id::integer AS product_id,
		product.number AS productnumber,
		product.unit AS productunit,
		CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (
			SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum
		) ELSE (
			SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum
		) END AS plannedquantity,
		trackingoperationproductincomponent.usedquantity AS usedquantity
	FROM productioncounting_trackingoperationproductincomponent trackingoperationproductincomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductincomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductincomponent.product_id
	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1
		ON (
			productioncountingquantity_1.order_id = productiontracking.order_id
			AND productioncountingquantity_1.product_id = trackingoperationproductincomponent.product_id
   			AND productioncountingquantity_1.role::text = '01used'::text
   		)
   	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2
   		ON (
   			productioncountingquantity_2.order_id = productiontracking.order_id
   			AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id
			AND productioncountingquantity_2.product_id = trackingoperationproductincomponent.product_id
			AND productioncountingquantity_2.role::text = '01used'::text
		)
	GROUP BY
		trackingoperationproductincomponent.id,
		productiontracking.id,
		product.id,
		product.number,
		product.unit,
		trackingoperationproductincomponent.usedquantity,
		productiontracking.technologyoperationcomponent_id;

-- end


-- productioncounting_trackingoperationproductoutcomponentdto
-- last touched 14.02.2016 by lupo

CREATE SEQUENCE productioncounting_trackingoperationproductoutcomponentdto_id_seq;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductoutcomponentdto AS
	SELECT
		trackingoperationproductoutcomponent.id AS id,
		productiontracking.id::integer AS productiontracking_id,
		product.id::integer AS product_id,
		product.number AS productnumber,
		product.unit AS productunit,
		CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (
			SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum
		) ELSE (
			SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum
		) END AS plannedquantity,
		trackingoperationproductoutcomponent.usedquantity AS usedquantity
	FROM productioncounting_trackingoperationproductoutcomponent trackingoperationproductoutcomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductoutcomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductoutcomponent.product_id
	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1
		ON (
			productioncountingquantity_1.order_id = productiontracking.order_id
			AND productioncountingquantity_1.product_id = trackingoperationproductoutcomponent.product_id
   			AND productioncountingquantity_1.role::text = '02produced'::text
   		)
   	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2
   		ON (
   			productioncountingquantity_2.order_id = productiontracking.order_id
   			AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id
			AND productioncountingquantity_2.product_id = trackingoperationproductoutcomponent.product_id
			AND productioncountingquantity_2.role::text = '02produced'::text
		)
	GROUP BY
		trackingoperationproductoutcomponent.id,
		productiontracking.id,
		product.id,
		product.number,
		product.unit,
		trackingoperationproductoutcomponent.usedquantity,
		productiontracking.technologyoperationcomponent_id;

-- end


-- productioncounting_trackingoperationproductcomponentdto
-- last touched 14.02.2016 by lupo

CREATE SEQUENCE productioncounting_trackingoperationproductcomponentdto_id_seq;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductcomponentdto AS
	SELECT
		row_number() OVER () AS id,
		trackingoperationproductcomponentdto.productiontracking_id::integer AS productiontracking_id,
    	trackingoperationproductcomponentdto.product_id::integer AS product_id,
    	trackingoperationproductcomponentdto.productnumber AS productnumber,
    	trackingoperationproductcomponentdto.productunit AS productunit,
    	trackingoperationproductcomponentdto.plannedquantity AS plannedquantity,
    	trackingoperationproductcomponentdto.usedquantity AS usedquantity
    FROM (
    	SELECT
    		trackingoperationproductincomponentdto.productiontracking_id,
			trackingoperationproductincomponentdto.product_id,
			trackingoperationproductincomponentdto.productnumber,
			trackingoperationproductincomponentdto.productunit,
			trackingoperationproductincomponentdto.plannedquantity,
			trackingoperationproductincomponentdto.usedquantity
        FROM productioncounting_trackingoperationproductincomponentdto trackingoperationproductincomponentdto
        UNION
		SELECT
			trackingoperationproductoutcomponentdto.productiontracking_id,
            trackingoperationproductoutcomponentdto.product_id,
            trackingoperationproductoutcomponentdto.productnumber,
            trackingoperationproductoutcomponentdto.productunit,
            trackingoperationproductoutcomponentdto.plannedquantity,
            trackingoperationproductoutcomponentdto.usedquantity
        FROM productioncounting_trackingoperationproductoutcomponentdto trackingoperationproductoutcomponentdto
    ) trackingoperationproductcomponentdto;

-- end


-- productioncounting_productiontrackingforproductdto
-- last touched 14.02.2016 by lupo

CREATE SEQUENCE productioncounting_productiontrackingforproductdto_id_seq;

CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductdto AS
	SELECT
		trackingoperationproductcomponentdto.id AS id,
		productiontrackingdto.number AS number,
		productiontrackingdto.state AS state,
		productiontrackingdto.createdate AS createdate,
		productiontrackingdto.lasttracking AS lasttracking,
		productiontrackingdto.timerangefrom AS timerangefrom,
		productiontrackingdto.timerangeto AS timerangeto,
		productiontrackingdto.active AS active,
		productiontrackingdto.order_id::integer AS order_id,
		productiontrackingdto.ordernumber AS ordernumber,
		productiontrackingdto.orderstate AS orderstate,
		productiontrackingdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id,
		productiontrackingdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber,
		productiontrackingdto.operation_id::integer AS operation_id,
		productiontrackingdto.shift_id::integer AS shift_id,
		productiontrackingdto.shiftname AS shiftname,
		productiontrackingdto.staff_id::integer AS staff_id,
		productiontrackingdto.staffname AS staffname,
		productiontrackingdto.division_id::integer AS division_id,
		productiontrackingdto.divisionnumber AS divisionnumber,
		productiontrackingdto.subcontractor_id::integer AS subcontractor_id,
		productiontrackingdto.subcontractorname AS subcontractorname,
		trackingoperationproductcomponentdto.product_id::integer AS product_id,
		trackingoperationproductcomponentdto.productnumber AS productnumber,
		trackingoperationproductcomponentdto.productunit AS productunit,
		trackingoperationproductcomponentdto.plannedquantity AS plannedquantity,
		trackingoperationproductcomponentdto.usedquantity AS usedquantity,
		productiontrackingdto.id::integer AS productiontracking_id
	FROM productioncounting_trackingoperationproductcomponentdto trackingoperationproductcomponentdto
	LEFT JOIN productioncounting_productiontrackingdto productiontrackingdto
		ON productiontrackingdto.id = trackingoperationproductcomponentdto.productiontracking_id;

-- end


-- productioncounting_productiontrackingforproductgroupeddto
-- last touched 14.02.2016 by lupo

CREATE SEQUENCE productioncounting_productiontrackingforproductgroupeddto_id_seq;

CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductgroupeddto AS
	SELECT
		row_number() OVER () AS id,
		productiontrackingforproductdto.active AS active,
		productiontrackingforproductdto.order_id::integer AS order_id,
    	productiontrackingforproductdto.ordernumber AS ordernumber,
    	productiontrackingforproductdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id,
    	productiontrackingforproductdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber,
    	productiontrackingforproductdto.operation_id AS operation_id,
    	productiontrackingforproductdto.product_id::integer AS product_id,
    	productiontrackingforproductdto.productnumber AS productnumber,
    	productiontrackingforproductdto.productunit AS productunit,
    	productiontrackingforproductdto.plannedquantity AS plannedquantity,
    	SUM(productiontrackingforproductdto.usedquantity) AS usedquantity
	FROM productioncounting_productiontrackingforproductdto productiontrackingforproductdto
	GROUP BY
		productiontrackingforproductdto.active,
		productiontrackingforproductdto.order_id,
		productiontrackingforproductdto.ordernumber,
		productiontrackingforproductdto.technologyoperationcomponent_id,
		productiontrackingforproductdto.technologyoperationcomponentnumber,
		productiontrackingforproductdto.operation_id,
		productiontrackingforproductdto.product_id,
		productiontrackingforproductdto.productnumber,
		productiontrackingforproductdto.productunit,
		productiontrackingforproductdto.plannedquantity;

-- end


-- qcadooview_view, qcadooview_item
-- last touched 15.02.2016 by lupo

INSERT INTO qcadooview_view(pluginidentifier, name, view, entityversion)
	VALUES (
		'productionCounting',
		'productionTrackingsForProductList',
		'productionTrackingsForProductList',
		0
	);

INSERT INTO qcadooview_item(pluginidentifier, name, active, category_id, view_id, succession, authrole, entityversion)
	VALUES (
		'productionCounting',
		'productionTrackingForProduct',
		true,
		(SELECT id FROM qcadooview_category WHERE name = 'ordersTracking' LIMIT 1),
		(SELECT id FROM qcadooview_view WHERE name = 'productionTrackingsForProductList' LIMIT 1),
		(SELECT max(succession) + 1 FROM qcadooview_item WHERE category_id = (SELECT id FROM qcadooview_category WHERE name = 'ordersTracking' LIMIT 1)),
		'ROLE_PRODUCTION_TRACKING',
		0
	);

INSERT INTO qcadooview_view(pluginidentifier, name, view, entityversion)
	VALUES (
		'productionCounting',
		'productionTrackingsForProductGroupedList',
		'productionTrackingsForProductGroupedList',
		0
	);

INSERT INTO qcadooview_item(pluginidentifier, name, active, category_id, view_id, succession, authrole, entityversion)
	VALUES (
		'productionCounting',
		'productionTrackingForProductGrouped',
		true,
		(SELECT id FROM qcadooview_category WHERE name = 'ordersTracking' LIMIT 1),
		(SELECT id FROM qcadooview_view WHERE name = 'productionTrackingsForProductGroupedList' LIMIT 1),
		(SELECT max(succession) + 1 FROM qcadooview_item WHERE category_id = (SELECT id FROM qcadooview_category WHERE name = 'ordersTracking' LIMIT 1)),
		'ROLE_PRODUCTION_TRACKING',
		0
	);

-- end


-- productioncounting_productiontrackingforproductgroupeddto
-- last touched 17.02.2016 by lupo

CREATE SEQUENCE cmmsmachineparts_maintenanceeventxlshelper_id_seq;

CREATE TABLE cmmsmachineparts_maintenanceeventxlshelper
(
  id bigint NOT NULL,
  query text,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT cmmsmachineparts_maintenanceeventxlshelper_pkey PRIMARY KEY (id)
);

-- end
