-- #GOODFOOD-1196
CREATE SEQUENCE cmmsmachineparts_maintenanceevent_number_seq;

-- add functions and trigger
CREATE OR REPLACE FUNCTION generate_maintenanceevent_number()
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
	_pattern := '#seq';

	select nextval('cmmsmachineparts_maintenanceevent_number_seq') into _sequence_value;

	_seq := to_char(_sequence_value, 'fm000000');
	if _seq like '%#%' then
		_seq := _sequence_value;
	end if;
	
	_number := _pattern;
	_number := replace(_number, '#seq', _seq);
	
	RETURN _number;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION generate_and_set_maintenanceevent_number_trigger()
  RETURNS trigger AS
$$
BEGIN
	NEW.number := generate_maintenanceevent_number();
	
	return NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER cmmsmachineparts_maintenanceevent_trigger_number
  BEFORE INSERT
  ON cmmsmachineparts_maintenanceevent
  FOR EACH ROW
  EXECUTE PROCEDURE generate_and_set_maintenanceevent_number_trigger();

-- migrate old data
CREATE OR REPLACE FUNCTION migrate_maintenanceevent_numbers()
  RETURNS void AS
$$
DECLARE
	row record;
	_number text;
BEGIN
    FOR row IN (select * from cmmsmachineparts_maintenanceevent order by id asc)
    LOOP
	_number := generate_maintenanceevent_number();
	update cmmsmachineparts_maintenanceevent set number = _number where id = row.id;    
            
    END LOOP;
END;
$$ LANGUAGE 'plpgsql';

select migrate_maintenanceevent_numbers();
drop function migrate_maintenanceevent_numbers();

alter table cmmsmachineparts_maintenanceevent alter column number set not null;
alter table cmmsmachineparts_maintenanceevent add unique(number);
-- end;

-- VIEW: technologies_technologydto
-- by kasi

CREATE SEQUENCE technologies_technologydto_id_seq;

CREATE OR REPLACE VIEW technologies_technologydto AS
 SELECT technology.id,
    technology.name,
    technology.number,
    technology.externalsynchronized,
    technology.master,
    technology.state,
    product.number AS productnumber,
    product.globaltypeofmaterial AS productglobaltypeofmaterial,
    tg.number AS technologygroupnumber,
    division.name AS divisionname,
    product.name AS productname,
    technology.technologytype,
    technology.active
   FROM technologies_technology technology
     LEFT JOIN basic_product product ON technology.product_id = product.id
     LEFT JOIN basic_division division ON technology.division_id = division.id
     LEFT JOIN technologies_technologygroup tg ON technology.technologygroup_id = tg.id;

-- end

-- added source cost report filter
-- last touched 17.03.2016 by pako

CREATE TABLE cmmsmachineparts_sourcecostreportfilter
(
  id bigint NOT NULL,
  fromdate date,
  todate date,
  sourcecost_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  dontsave boolean,
  CONSTRAINT cmmsmachineparts_sourcecostreportfilter_pkey PRIMARY KEY (id),
  CONSTRAINT sourcecostreportfilter_sourcecost_fkey FOREIGN KEY (sourcecost_id)
      REFERENCES cmmsmachineparts_sourcecost (id) DEFERRABLE
)

-- end
