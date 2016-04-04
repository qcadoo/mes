-- last touched 29.03.2016 by kasi

ALTER TABLE technologies_operationproductincomponent ADD COLUMN quantityformula character varying(255);

-- end


-- #NBLS-160

ALTER TABLE basic_parameter ADD COLUMN consumptionOfRawMaterialsBasedOnStandards boolean default false;

-- end


-- Added new role for production registration terminal
-- last touched 01.04.2016 by lupo

SELECT add_role('ROLE_PRODUCTION_REGISTRATION_TERMINAL','Dostęp do terminalu rejestracji produkcji');
SELECT add_group_role('SUPER_ADMIN', 'ROLE_PRODUCTION_REGISTRATION_TERMINAL');
SELECT add_group_role('ADMIN', 'ROLE_PRODUCTION_REGISTRATION_TERMINAL');
SELECT add_group_role('USER', 'ROLE_PRODUCTION_REGISTRATION_TERMINAL');

-- end
