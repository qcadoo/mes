
-- update date from in workstation type components
-- last touched 22.02.2016 by kama

ALTER TABLE productionlines_workstationtypecomponent ADD COLUMN datefrom timestamp without time zone;
ALTER TABLE productionlines_workstationtypecomponent ADD COLUMN dateto timestamp without time zone;

UPDATE productionlines_workstationtypecomponent SET datefrom = '1970-01-01 00:00:00' WHERE datefrom IS NULL;

-- end


-- #QCADOO-433
-- add name field

alter table materialflowresources_document  add column name character varying(255);

-- add functions and trigger

CREATE OR REPLACE FUNCTION generate_document_number(_translated_type text)
  RETURNS text AS
$$
DECLARE
	_pattern text;
	_sequence_name text;
	_sequence_value numeric;
	_tmp text;
	_seq text;
	_number text;
BEGIN
	_pattern := '#translated_type/#seq';

	_sequence_name := 'materialflowresources_document_number_' || lower(_translated_type);

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
	_number := replace(_number, '#translated_type', _translated_type);
	_number := replace(_number, '#seq', _seq);

	RETURN _number;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION generate_and_set_document_number_trigger()
  RETURNS trigger AS
$$
BEGIN
	NEW.number := generate_document_number(NEW.number);
	IF NEW.name is null THEN
		NEW.name := NEW.number;
	END IF;

	return NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER materialflowresources_document_trigger_number
  BEFORE INSERT
  ON materialflowresources_document
  FOR EACH ROW
  EXECUTE PROCEDURE generate_and_set_document_number_trigger();

-- migrate old data

CREATE OR REPLACE FUNCTION migrate_document_numbers()
  RETURNS void AS
$$
DECLARE
	row record;
	_number text;
BEGIN
    FOR row IN (select x.type,  CASE WHEN type='01receipt' THEN 'PZ'
            WHEN type='02internalInbound' THEN 'PW'
            WHEN type='03internalOutbound' THEN 'RW'
            WHEN type='04release' THEN 'WZ'
            WHEN type='05transfer' THEN 'MM'
       END as translated_type,* from materialflowresources_document x where
	(order_id is null and number in (select number from materialflowresources_document group by number having count(number)>1))
	OR (number like '~~~%')
	)
    LOOP
	_number := generate_document_number(row.translated_type);
	update materialflowresources_document set number = _number, name = _number where id = row.id;

    END LOOP;

    update materialflowresources_document set name = number where name is null;
END;
$$ LANGUAGE 'plpgsql';

select migrate_document_numbers();
drop function migrate_document_numbers();


alter table materialflowresources_document alter column name set not null;
alter table materialflowresources_document alter column type set not null;

alter table materialflowresources_document alter column number set not null;
alter table materialflowresources_document add unique(number);

-- end #QCADOO-433


-- Table: qcadooplugin_plugin
-- by kasi

ALTER TABLE qcadooplugin_plugin ADD COLUMN license character varying(255);
update qcadooplugin_plugin set version = '1.3.0';

-- end


-- start

drop view productioncounting_productiontrackingforproductgroupeddto;
drop view productioncounting_productiontrackingforproductdto;
drop view productioncounting_trackingoperationproductcomponentdto;
drop view productioncounting_trackingoperationproductoutcomponentdto;
drop view productioncounting_trackingoperationproductincomponentdto;
drop view productioncounting_productiontrackingdto;

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
		trackingoperationproductincomponent.usedquantity AS usedquantity,
        batch.number AS batchnumber
	FROM productioncounting_trackingoperationproductincomponent trackingoperationproductincomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductincomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductincomponent.product_id
	LEFT JOIN advancedgenealogy_batch batch ON batch.id = trackingoperationproductincomponent.batch_id
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
		productiontracking.technologyoperationcomponent_id,
		batch.number;


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
		trackingoperationproductoutcomponent.usedquantity AS usedquantity,
        batch.number AS batchnumber
	FROM productioncounting_trackingoperationproductoutcomponent trackingoperationproductoutcomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductoutcomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductoutcomponent.product_id
	LEFT JOIN advancedgenealogy_batch batch ON batch.id = trackingoperationproductoutcomponent.batch_id
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
		productiontracking.technologyoperationcomponent_id,
		batch.number;


CREATE OR REPLACE VIEW productioncounting_trackingoperationproductcomponentdto AS
	SELECT
		row_number() OVER () AS id,
		trackingoperationproductcomponentdto.productiontracking_id::integer AS productiontracking_id,
    	trackingoperationproductcomponentdto.product_id::integer AS product_id,
    	trackingoperationproductcomponentdto.productnumber AS productnumber,
    	trackingoperationproductcomponentdto.productunit AS productunit,
    	trackingoperationproductcomponentdto.plannedquantity AS plannedquantity,
    	trackingoperationproductcomponentdto.usedquantity AS usedquantity,
        trackingoperationproductcomponentdto.batchnumber
    FROM (
    	SELECT
    		trackingoperationproductincomponentdto.productiontracking_id,
			trackingoperationproductincomponentdto.product_id,
			trackingoperationproductincomponentdto.productnumber,
			trackingoperationproductincomponentdto.productunit,
			trackingoperationproductincomponentdto.plannedquantity,
			trackingoperationproductincomponentdto.usedquantity,
            trackingoperationproductincomponentdto.batchnumber
        FROM productioncounting_trackingoperationproductincomponentdto trackingoperationproductincomponentdto
        UNION
		SELECT
			trackingoperationproductoutcomponentdto.productiontracking_id,
            trackingoperationproductoutcomponentdto.product_id,
            trackingoperationproductoutcomponentdto.productnumber,
            trackingoperationproductoutcomponentdto.productunit,
            trackingoperationproductoutcomponentdto.plannedquantity,
            trackingoperationproductoutcomponentdto.usedquantity,
            trackingoperationproductoutcomponentdto.batchnumber
        FROM productioncounting_trackingoperationproductoutcomponentdto trackingoperationproductoutcomponentdto
    ) trackingoperationproductcomponentdto;


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
		productiontrackingdto.id::integer AS productiontracking_id,
        trackingoperationproductcomponentdto.batchnumber
	FROM productioncounting_trackingoperationproductcomponentdto trackingoperationproductcomponentdto
	LEFT JOIN productioncounting_productiontrackingdto productiontrackingdto
		ON productiontrackingdto.id = trackingoperationproductcomponentdto.productiontracking_id;


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


-- alerts
-- by kasi

CREATE TABLE qcadooview_alert
(
  id bigint NOT NULL,
  message text,
  type character varying(255) DEFAULT 'information'::character varying,
  expirationdate timestamp without time zone,
  sound boolean DEFAULT false,
  CONSTRAINT qcadooview_alert_pkey PRIMARY KEY (id)
);

CREATE TABLE qcadooview_viewedalert
(
  id bigint NOT NULL,
  user_id bigint,
  alert_id bigint,
  CONSTRAINT qcadooview_viewedalert_pkey PRIMARY KEY (id),
  CONSTRAINT viewedalert_alert_fkey FOREIGN KEY (alert_id)
      REFERENCES qcadooview_alert (id) DEFERRABLE,
  CONSTRAINT alert_user_fkey FOREIGN KEY (user_id)
      REFERENCES qcadoosecurity_user (id) DEFERRABLE
);

--
