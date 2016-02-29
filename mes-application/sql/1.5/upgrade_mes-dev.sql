
-- update date from in workstation type components
-- last touched 22.02.2016 by kama

ALTER TABLE productionlines_workstationtypecomponent ADD COLUMN datefrom timestamp without time zone;
ALTER TABLE productionlines_workstationtypecomponent ADD COLUMN dateto timestamp without time zone;

UPDATE productionlines_workstationtypecomponent SET datefrom = '1970-01-01 00:00:00' WHERE datefrom IS NULL;

-- end
