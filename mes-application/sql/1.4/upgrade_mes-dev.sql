
ALTER TABLE basic_division ADD COLUMN productionline_id bigint;
ALTER TABLE basic_division ADD CONSTRAINT division_productionline_fkey FOREIGN KEY (productionline_id)
     REFERENCES productionlines_productionline (id) DEFERRABLE;

ALTER TABLE basic_workstation DROP CONSTRAINT workstation_division_fkey;
ALTER TABLE basic_workstation DROP COLUMN division_id;

DROP TABLE jointable_division_workstationtype;

ALTER TABLE technologies_operation ADD COLUMN division_id bigint;
ALTER TABLE technologies_operation ADD CONSTRAINT operation_division_fkey FOREIGN KEY (division_id)
     REFERENCES basic_division (id) DEFERRABLE;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN division_id bigint;
ALTER TABLE technologies_technologyoperationcomponent ADD CONSTRAINT technologyoperationcomponent_division_fkey FOREIGN KEY (division_id)
     REFERENCES basic_division (id) DEFERRABLE;

ALTER TABLE technologies_operationproductincomponent ADD COLUMN priority int;

ALTER TABLE basic_division ADD COLUMN parameter_id bigint;
ALTER TABLE basic_division ADD CONSTRAINT division_parameter_fkey FOREIGN KEY (parameter_id)
     REFERENCES basic_parameter (id) DEFERRABLE;

ALTER TABLE basic_product ADD COLUMN showinproductdata boolean;
ALTER TABLE basic_product ALTER COLUMN showinproductdata SET DEFAULT false;

-- end