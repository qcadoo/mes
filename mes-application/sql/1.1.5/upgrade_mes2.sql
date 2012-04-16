
-- Table: technologies_technologygroup
-- changed: 03.04.2012

CREATE TABLE technologies_technologygroup
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(2048),
  active boolean DEFAULT true,
  CONSTRAINT technologies_technologygroup_pkey PRIMARY KEY (id)
);

-- end


-- Table: technologies_technology
-- changed: 03.04.2012

ALTER TABLE technologies_technology ADD COLUMN technologygroup_id bigint;
ALTER TABLE technologies_technology
  ADD CONSTRAINT technologies_technology_technologygroup_fkey FOREIGN KEY (technologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE;
      
-- end

-- Table: technologies_operation
-- changed: 03.04.2012

ALTER TABLE technologies_operation ADD COLUMN areproductquantitiesdivisible boolean DEFAULT true;
ALTER TABLE technologies_operation ADD COLUMN istjdivisible boolean DEFAULT true;

-- end

-- Table: technologies_technologyoperationcomponent
-- changed: 03.04.2012

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN areproductquantitiesdivisible boolean DEFAULT true;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN istjdivisible boolean DEFAULT true;

-- end

CREATE TABLE productionlines_productionline
(
  id bigint NOT NULL,
  "number" character varying(255),
  "name" character varying(2048),
  division_id bigint,
  place character varying(255),
  description character varying(2048),
  supportsalltechnologies boolean DEFAULT true,
  documentation character varying(255),
  active boolean DEFAULT true,
  CONSTRAINT productionlines_productionline_pkey PRIMARY KEY (id),
  CONSTRAINT productionlines_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE
);

CREATE TABLE jointable_productionline_technology
(
  technology_id bigint NOT NULL,
  productionline_id bigint NOT NULL,
  CONSTRAINT jointable_productionline_technology_pkey PRIMARY KEY (productionline_id, technology_id),
  CONSTRAINT jointable_pl_tech_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT jointable_pl_tech_technology_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE
);

CREATE TABLE jointable_productionline_technologygroup
(
  technologygroup_id bigint NOT NULL,
  productionline_id bigint NOT NULL,
  CONSTRAINT jointable_productionline_technologygroup_pkey PRIMARY KEY (productionline_id, technologygroup_id),
  CONSTRAINT jointable_pl_techgroup_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT jointable_pl_techgroup_technologygroup_fkey FOREIGN KEY (technologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE
);

-- Table: basic_product
-- changed: 04.04.2012

ALTER TABLE basic_product ADD COLUMN technologygroup_id bigint;
ALTER TABLE basic_product
  ADD CONSTRAINT basic_product_technologygroup_fkey FOREIGN KEY (technologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE;
      
-- end

-- Table: basic_product
-- changed: 05.04.2012
ALTER TABLE productionScheduling_orderOperationComponent RENAME TO technologies_technologyInstanceOperationComponent;
-- end

-- Table: costnormsforproduct_orderoperationproductincomponent
-- changed: 16.04.2012

ALTER TABLE costnormsforproduct_orderoperationproductincomponent RENAME TO costnormsformaterials_orderoperationproductincomponent;
--end

-- Table: costnormsformaterials_orderoperationproductincomponent
--changed: 11.04.2012
ALTER TABLE costnormsformaterials_orderoperationproductincomponent RENAME TO costnormsformaterials_technologyinstoperproductincomp;
--end

-- Table: workplans_orderoperationoutputcolumn
-- changed: 11.04.2012
ALTER TABLE workplans_orderoperationoutputcolumn RENAME COLUMN orderOperationComponent_id TO technologyInstanceOperationComponent_id;
-- end

-- Table: workplans_orderoperationinputcolumn
-- changed: 11.04.2012
ALTER TABLE workplans_orderoperationinputcolumn RENAME COLUMN orderOperationComponent_id TO technologyInstanceOperationComponent_id;
-- end

-- Table: productioncountingwithcosts_orderoperationproductincomponent
--changed: 11.04.2012
ALTER TABLE productioncountingwithcosts_orderoperationproductincomponent RENAME TO productioncountingwithcosts_technologyInstOperProductInComp;
--end

-- Table: productioncountingwithcosts_operationpieceworkcostcomponent
-- changed: 11.04.2012
ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent RENAME COLUMN orderOperationComponent_id TO technologyInstanceOperationComponent_id;
-- end

-- Table: productioncountingwithcosts_operationcostcomponent
-- changed: 11.04.2012
ALTER TABLE productioncountingwithcosts_operationcostcomponent RENAME COLUMN orderOperationComponent_id TO technologyInstanceOperationComponent_id;
-- end

-- Table: productioncounting_productionrecord
-- changed: 11.04.2012
ALTER TABLE productioncounting_productionrecord RENAME COLUMN orderOperationComponent_id TO technologyInstanceOperationComponent_id;
-- end

-- Table: productioncounting_operationtimecomponent
-- changed: 11.04.2012
ALTER TABLE productioncounting_operationtimecomponent RENAME COLUMN orderOperationComponent_id TO technologyInstanceOperationComponent_id;
-- end

-- Table: productioncounting_operationpieceworkcomponent
-- changed: 11.04.2012
ALTER TABLE productioncounting_operationpieceworkcomponent RENAME COLUMN orderOperationComponent_id TO technologyInstanceOperationComponent_id;
-- end


-- Table: productionlines_productionline
-- changed: 12.04.2012

ALTER TABLE productionlines_productionline ADD COLUMN supportsothertechnologiesworkstationtypes boolean DEFAULT true;
ALTER TABLE productionlines_productionline ADD COLUMN quantityForOtherWorkstationtypes integer;

-- end


-- Table: productionlines_workstationtypecomponent
-- changed: 12.04.2012

CREATE TABLE productionlines_workstationtypecomponent
(
  id bigint NOT NULL,
  workstationtype_id bigint,
  productionline_id bigint,
  quantity integer,
  CONSTRAINT productionlines_workstationtypecomponent_pkey PRIMARY KEY (id),
  CONSTRAINT productionlines_workstationtypecomponent_wt_fkey FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) DEFERRABLE,
  CONSTRAINT productionlines_workstationtypecomponent_pl_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE
);

-- end


-- Table: technologies_technologyinstanceoperationcomponent
-- changed: 11.04.2012
ALTER TABLE technologies_operation RENAME COLUMN countMachineOperation TO countMachine;
ALTER TABLE technologies_operation RENAME COLUMN countRealizedOperation TO countRealized;
-- end


-- Table:  technologies_technologyinstanceoperationcomponent
-- changed: 12.04.2012

ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN quantityofworkstationtypes integer;

-- end

ALTER TABLE costcalculation_costcalculation ADD COLUMN productionline_id bigint;
ALTER TABLE costcalculation_costcalculation ADD CONSTRAINT costcalculation_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE;

-- Table: costnormsformaterials_calculationoperationcomponent
-- changed: 16.04.2012
ALTER TABLE costnormsformaterials_calculationoperationcomponent  RENAME TO costnormsforoperation_calculationoperationcomponent;

--end

--Table : costnormsforoperation_calculationoperationcomponent
-- changed: 16.04.2012
ALTER TABLE costnormsforoperation_calculationoperationcomponent ADD COLUMN areProductQuantitiesDivisible boolean DEFAULT true;
ALTER TABLE costnormsforoperation_calculationoperationcomponent ADD COLUMN isTjDivisible boolean DEFAULT true;
--end


--Table : orders_order
-- changed: 16.04.2012
ALTER TABLE orders_order ADD COLUMN generatedenddate timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN operationdurationquantityunit character varying(255);
ALTER TABLE orders_order ADD COLUMN effectivetimeconsumption integer;
--end

--Table : technologies_operation
-- changed: 16.04.2012
ALTER TABLE technologies_operation ADD COLUMN countMachineUNIT character varying(255);
ALTER TABLE technologies_operation ADD COLUMN productionInOneCycleUNIT character varying(255);

--end

--Table : technologies_technologyoperationcomponent
-- changed: 16.04.2012
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN countMachineUNIT character varying(255);
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN productionInOneCycleUNIT character varying(255);
--end

--Table : technologies_technologyinstanceoperationcomponent
-- changed: 16.04.2012
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN countMachineUNIT character varying(255);
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN productionInOneCycleUNIT character varying(255);
--end

-- Table: technologies_technologyinstanceoperationcomponent
-- changed: 16.04.2012
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN areproductquantitiesdivisible boolean DEFAULT true;
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN istjdivisible boolean DEFAULT true;
-- end


-- Table: orders_order
-- changed: 16.04.2012

ALTER TABLE orders_order ADD COLUMN productionline_id bigint;
ALTER TABLE orders_order
  ADD CONSTRAINT orders_order_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE;
      
ALTER TABLE orders_order ADD COLUMN deadline timestamp without time zone;

-- end


-- Table: basic_parameter
-- changed: 16.04.2012

ALTER TABLE basic_parameter ADD COLUMN defaultproductionline_id bigint;
ALTER TABLE basic_parameter
  ADD CONSTRAINT basic_parameter_defaultproductionline_fkey FOREIGN KEY (defaultproductionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE;
      
-- end
