-- Table: basic_parameter
-- changed: 09.01.2013

ALTER TABLE basic_parameter RENAME canchangedatewhentransfertowarehouse TO changedatewhentransfertowarehousetype;
ALTER TABLE basic_parameter ALTER COLUMN changedatewhentransfertowarehousetype TYPE character varying(255);
ALTER TABLE basic_parameter ALTER COLUMN changedatewhentransfertowarehousetype SET DEFAULT '01never'::character varying;

UPDATE basic_parameter SET changedatewhentransfertowarehousetype = '01never';

-- end
