-- added parameter
-- last touched 29.08.2016 by kama

ALTER TABLE basic_parameter ADD COLUMN deliveredbiggerthanordered boolean DEFAULT true;

-- end