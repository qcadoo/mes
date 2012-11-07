-- Table: basic_parameter
-- changed: 07.11.2012

ALTER TABLE basic_parameter ADD COLUMN canchangedatewhentransfertowarehouse boolean;
ALTER TABLE basic_parameter ALTER COLUMN canchangedatewhentransfertowarehouse SET DEFAULT false;

-- end
