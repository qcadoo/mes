-- Table: productioncounting_productionbalance
-- changed: 29.03.2012

ALTER TABLE productioncounting_productionbalance ADD COLUMN plannedcyclescosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN cyclescosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN cyclescostsbalance numeric(10,3);

-- end


-- Table: productioncounting_operationpieceworkcomponent
-- changed: 29.03.2012

CREATE TABLE productioncounting_operationpieceworkcomponent
(
  id bigint NOT NULL,
  productionbalance_id bigint,
  orderoperationcomponent_id bigint,
  plannedcycles numeric(10,3),
  cycles numeric(10,3),
  cyclesbalance numeric(10,3),
  CONSTRAINT productioncounting_operationpieceworkc_pkey PRIMARY KEY (id ),
  CONSTRAINT productioncounting_operationpieceworkc_ooc_fkey FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) DEFERRABLE,
  CONSTRAINT productioncounting_operationpieceworkc_pb_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE
);

-- end


-- Table: productioncountingwithcosts_operationpieceworkcostcomponent
-- changed: 29.03.2012

CREATE TABLE productioncountingwithcosts_operationpieceworkcostcomponent
(
  id bigint NOT NULL,
  productionbalance_id bigint,
  orderoperationcomponent_id bigint,
  plannedcyclescosts numeric(10,3),
  cyclescosts numeric(10,3),
  cyclescostsbalance numeric(10,3),
  CONSTRAINT productioncountingwithcosts_operationpieceworkcc_pkey PRIMARY KEY (id ),
  CONSTRAINT productioncountingwithcosts_operationpieceworkcc_ooc_fkey FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) DEFERRABLE,
  CONSTRAINT productioncountingwithcosts_operationpieceworkcc_pb_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE
);

-- end


-- Table: workplans_columnforinputproducts
-- changed: 30.03.2012

ALTER TABLE workplans_columnforinputproducts ALTER COLUMN name TYPE character varying(1024);
ALTER TABLE workplans_columnforinputproducts ALTER COLUMN description TYPE character varying(1024);
ALTER TABLE workplans_columnforinputproducts ALTER COLUMN columnfiller TYPE character varying(255);

ALTER TABLE workplans_columnforinputproducts ADD COLUMN alignment character varying(255);
ALTER TABLE workplans_columnforinputproducts ALTER COLUMN alignment SET DEFAULT '01left'::character varying;

UPDATE workplans_columnforinputproducts SET alignment = '01left' WHERE identifier = 'productName'; 
UPDATE workplans_columnforinputproducts SET alignment = '02right' WHERE identifier = 'plannedQuantity'; 
UPDATE workplans_columnforinputproducts SET alignment = '02right' WHERE identifier = 'batchNumbers'; 

-- end


-- Table: workplans_columnforoutputproducts
-- changed: 30.03.2012

ALTER TABLE workplans_columnforoutputproducts ALTER COLUMN name TYPE character varying(1024);
ALTER TABLE workplans_columnforoutputproducts ALTER COLUMN description TYPE character varying(1024);
ALTER TABLE workplans_columnforoutputproducts ALTER COLUMN columnfiller TYPE character varying(255);

ALTER TABLE workplans_columnforoutputproducts ADD COLUMN alignment character varying(255);
ALTER TABLE workplans_columnforoutputproducts ALTER COLUMN alignment SET DEFAULT '01left'::character varying;

UPDATE workplans_columnforoutputproducts SET alignment = '01left' WHERE identifier = 'productName'; 
UPDATE workplans_columnforoutputproducts SET alignment = '02right' WHERE identifier = 'plannedQuantity'; 

-- end


-- Table: workplans_columnfororders
-- changed: 30.03.2012

ALTER TABLE workplans_columnfororders ALTER COLUMN name TYPE character varying(1024);
ALTER TABLE workplans_columnfororders ALTER COLUMN description TYPE character varying(1024);
ALTER TABLE workplans_columnfororders ALTER COLUMN columnfiller TYPE character varying(255);

ALTER TABLE workplans_columnfororders ADD COLUMN alignment character varying(255);
ALTER TABLE workplans_columnfororders ALTER COLUMN alignment SET DEFAULT '01left'::character varying;

UPDATE workplans_columnfororders SET alignment = '01left'; 
UPDATE workplans_columnfororders SET alignment = '02right' WHERE identifier='plannedQuantity'; 

-- end


-- Table: productioncounting_productionrecord
-- changed: 02.04.2012

ALTER TABLE productioncounting_productionrecord ALTER COLUMN executedoperationcycles TYPE numeric(10,3);

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
 
