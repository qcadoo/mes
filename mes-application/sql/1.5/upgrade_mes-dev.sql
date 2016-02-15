
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
		productiontracking.id::integer AS id,
		productiontracking.number AS productiontrackingnumber,
		productiontracking.state AS productiontrackingstate,
		productiontracking.createdate AS productiontrackingcreatedate,
		productiontracking.lasttracking AS productiontrackinglasttracking,
		productiontracking.timerangefrom AS productiontrackingtimerangefrom,
		productiontracking.timerangeto AS productiontrackingtimerangeto,
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
		subcontractor.id AS subcontractor_id,
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
