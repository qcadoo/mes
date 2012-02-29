-- Table: costcalculation_costcalculation
-- changed: 29.02.2012
ALTER TABLE costcalculation_costcalculation ADD COLUMN sourceofmaterialcosts character varying(255);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN sourceofmaterialcosts SET DEFAULT '01currentGlobalDefinitionsInProduct'::character varying;

ALTER TABLE costcalculation_costcalculation ADD COLUMN printcostnormsofmaterials boolean;
ALTER TABLE costcalculation_costcalculation ALTER COLUMN printcostnormsofmaterials SET DEFAULT true;

ALTER TABLE costcalculation_costcalculation ADD COLUMN printoperationnorms boolean;
ALTER TABLE costcalculation_costcalculation ALTER COLUMN printoperationnorms SET DEFAULT true;
-- end