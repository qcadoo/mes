
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
       END as translated_type,* from materialflowresources_document x where order_id is null and number in (select number from materialflowresources_document group by number having count(number)>1))
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

--end #QCADOO-433
