-- Table: orders_order
-- changed: 28.02.2012

UPDATE basic_parameter SET batchnumberuniqueness='02supplier' WHERE batchnumberuniqueness='02manufacturer';
-- end

-- Table: costcalculation_costcalculation
-- changed: 29.02.2012
ALTER TABLE costcalculation_costcalculation ADD COLUMN sourceofmaterialcosts character varying(255);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN sourceofmaterialcosts SET DEFAULT '01currentGlobalDefinitionsInProduct'::character varying;

ALTER TABLE costcalculation_costcalculation ADD COLUMN printcostnormsofmaterials boolean;
ALTER TABLE costcalculation_costcalculation ALTER COLUMN printcostnormsofmaterials SET DEFAULT true;

ALTER TABLE costcalculation_costcalculation ADD COLUMN printoperationnorms boolean;
ALTER TABLE costcalculation_costcalculation ALTER COLUMN printoperationnorms SET DEFAULT true;
