-- last touched 29.03.2016 by kasi

ALTER TABLE technologies_operationproductincomponent ADD COLUMN quantityformula character varying(255);

-- end

-- #NBLS-160
ALTER TABLE basic_parameter ADD COLUMN consumptionOfRawMaterialsBasedOnStandards boolean default false;
-- end
