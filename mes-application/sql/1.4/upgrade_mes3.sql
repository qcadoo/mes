-- table: basic_division
-- changed: 03.10.2014 by lupo

ALTER TABLE basic_division
	DROP CONSTRAINT division_componentslocation_fkey;

ALTER TABLE basic_division
	ADD CONSTRAINT division_componentslocation_fkey FOREIGN KEY (componentslocation_id) REFERENCES materialflow_location (id) DEFERRABLE;
 
ALTER TABLE basic_division
	DROP CONSTRAINT division_componentsoutputlocation_fkey;
	
ALTER TABLE basic_division
	ADD CONSTRAINT division_componentsoutputlocation_fkey FOREIGN KEY (componentsoutputlocation_id) REFERENCES materialflow_location (id) DEFERRABLE;
	
ALTER TABLE basic_division
	DROP CONSTRAINT division_productsinputlocation_fkey;
 
ALTER TABLE basic_division
	ADD CONSTRAINT division_productsinputlocation_fkey FOREIGN KEY (productsinputlocation_id) REFERENCES materialflow_location (id) DEFERRABLE;

-- end


-- table: technologies_technology
-- changed: 03.10.2014 by lupo

ALTER TABLE technologies_technology
	DROP CONSTRAINT technology_division_fkey;

ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_division_fkey FOREIGN KEY (division_id) REFERENCES basic_division (id) DEFERRABLE;
	
ALTER TABLE technologies_technology
	DROP CONSTRAINT technology_componentslocation_fkey;
	
ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_componentslocation_fkey FOREIGN KEY (componentslocation_id) REFERENCES materialflow_location (id) DEFERRABLE;
 
ALTER TABLE technologies_technology
	DROP CONSTRAINT technology_componentsoutputlocation_fkey;
	
ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_componentsoutputlocation_fkey FOREIGN KEY (componentsoutputlocation_id) REFERENCES materialflow_location (id) DEFERRABLE;

ALTER TABLE technologies_technology
	DROP CONSTRAINT technology_productsinputlocation_fkey;
	
ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_productsinputlocation_fkey FOREIGN KEY (productsinputlocation_id) REFERENCES materialflow_location (id) DEFERRABLE;
 
-- end


-- table: technologies_operationproductincomponent
-- changed: 03.10.2014 by lupo

ALTER TABLE technologies_operationproductincomponent
	DROP CONSTRAINT technology_componentslocation_fkey;
	
ALTER TABLE technologies_operationproductincomponent
	ADD CONSTRAINT technology_componentslocation_fkey FOREIGN KEY (componentslocation_id) REFERENCES materialflow_location (id) DEFERRABLE;
 
ALTER TABLE technologies_operationproductincomponent
	DROP CONSTRAINT technology_componentsoutputlocation_fkey;
	
ALTER TABLE technologies_operationproductincomponent
	ADD CONSTRAINT technology_componentsoutputlocation_fkey FOREIGN KEY (componentsoutputlocation_id) REFERENCES materialflow_location (id) DEFERRABLE;
 
-- end


-- table: technologies_operationproductoutcomponent
-- changed: 03.10.2014 by lupo
 
ALTER TABLE technologies_operationproductoutcomponent
	DROP CONSTRAINT technology_productsinputlocation_fkey;
	
ALTER TABLE technologies_operationproductoutcomponent
	ADD CONSTRAINT technology_productsinputlocation_fkey FOREIGN KEY (productsinputlocation_id) REFERENCES materialflow_location (id) DEFERRABLE;
 
-- end


-- table: technologies_technologyinstanceoperationcomponent
-- changed: 03.10.2014 by lupo

DROP TABLE technologies_technologyinstanceoperationcomponent CASCADE;

-- end
