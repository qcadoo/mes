-- added set to product components
-- last touched 30.03.2016 by kama

ALTER TABLE technologies_operationproductoutcomponent ADD COLUMN set boolean;
ALTER TABLE technologies_operationproductoutcomponent ALTER COLUMN set SET DEFAULT false;

-- end