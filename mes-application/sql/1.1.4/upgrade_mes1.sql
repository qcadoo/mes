-- PBC1
ALTER TABLE orders_order add column registerpiecework boolean;
ALTER TABLE basic_parameter add column registerpiecework boolean;
update basic_parameter set registerpiecework=true;
ALTER TABLE productioncounting_productionrecord add column executedOperationCycles integer;

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

ALTER TABLE costcalculation_costcalculation ADD COLUMN includeadditionaltime boolean;
ALTER TABLE costcalculation_costcalculation ALTER COLUMN includeadditionaltime SET DEFAULT false;

-- end


-- Table: costcalculation_costcalculation
-- changed: 08.03.2012
      
ALTER TABLE costcalculation_costcalculation RENAME COLUMN dateofcalculation TO date;

ALTER TABLE costcalculation_costcalculation ALTER COLUMN calculateOperationCostsMode SET DEFAULT '01hourly';
UPDATE costcalculation_costcalculation SET calculateOperationCostsMode = '01hourly' WHERE calculateOperationCostsMode = 'hourly';
UPDATE costcalculation_costcalculation SET calculateOperationCostsMode = '02piecework' WHERE calculateOperationCostsMode = 'piecework';

ALTER TABLE costcalculation_costcalculation RENAME COLUMN costperunit TO totalcostperunit;

-- end


-- Table: costnormsforoperation_calculationoperationcomponent
-- changed: 08.03.2012

ALTER TABLE costnormsforoperation_calculationoperationcomponent ADD COLUMN productionbalance_id bigint;
ALTER TABLE costnormsforoperation_calculationoperationcomponent ADD CONSTRAINT calculationoperationcomponent_productionbalance_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE;
      
-- end


-- Table: productioncounting_productionbalance
-- changed: 12.03.2012

ALTER TABLE productioncounting_productionbalance ADD COLUMN printoperationnorms boolean;
ALTER TABLE productioncounting_productionbalance ALTER COLUMN printoperationnorms SET DEFAULT true;

ALTER TABLE productioncounting_productionbalance ADD COLUMN calculateoperationcostsmode character varying(255);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN calculateoperationcostsmode SET DEFAULT 'hourly'::character varying;

ALTER TABLE productioncounting_productionbalance ADD COLUMN includetpz boolean;
ALTER TABLE productioncounting_productionbalance ALTER COLUMN includetpz SET DEFAULT true;

ALTER TABLE productioncounting_productionbalance ADD COLUMN includeadditionaltime boolean;
ALTER TABLE productioncounting_productionbalance ALTER COLUMN includeadditionaltime SET DEFAULT false;

ALTER TABLE productioncounting_productionbalance ADD COLUMN plannedmachinetime integer;
ALTER TABLE productioncounting_productionbalance ADD COLUMN machinetime integer;
ALTER TABLE productioncounting_productionbalance ADD COLUMN machinetimebalance integer;

ALTER TABLE productioncounting_productionbalance ADD COLUMN plannedlabortime integer;
ALTER TABLE productioncounting_productionbalance ADD COLUMN labortime integer;
ALTER TABLE productioncounting_productionbalance ADD COLUMN labortimebalance integer;

ALTER TABLE productioncounting_productionbalance ADD COLUMN printcostnormsofmaterials boolean;
ALTER TABLE productioncounting_productionbalance ALTER COLUMN printcostnormsofmaterials SET DEFAULT true;

ALTER TABLE productioncounting_productionbalance ADD COLUMN sourceofmaterialcosts character varying(255);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN sourceofmaterialcosts SET DEFAULT '01currentGlobalDefinitionsInProduct'::character varying;

ALTER TABLE productioncounting_productionbalance ADD COLUMN calculatematerialcostsmode character varying(255);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN calculatematerialcostsmode SET DEFAULT '01nominal'::character varying;

ALTER TABLE productioncounting_productionbalance ADD COLUMN averagemachinehourlycost numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN averagelaborhourlycost numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN productioncostmargin numeric(10,3);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN productioncostmargin SET DEFAULT 0::numeric;

ALTER TABLE productioncounting_productionbalance ADD COLUMN materialcostmargin numeric(10,3);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN materialcostmargin SET DEFAULT 0::numeric;

ALTER TABLE productioncounting_productionbalance ADD COLUMN additionaloverhead numeric(10,3);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN additionaloverhead SET DEFAULT 0::numeric;

ALTER TABLE productioncounting_productionbalance ADD COLUMN plannedcomponentscosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN componentscosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN componentscostsbalance numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN plannedmachinecosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN machinecosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN machinecostsbalance numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN plannedlaborcosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN laborcosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN laborcostsbalance numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN registeredtotaltechnicalproductioncosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN registeredtotaltechnicalproductioncostperunit numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN totaltechnicalproductioncosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN totaltechnicalproductioncostperunit numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN balancetechnicalproductioncosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN balancetechnicalproductioncostperunit numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN productioncostmarginvalue numeric(10,3);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN productioncostmarginvalue SET DEFAULT 0::numeric;

ALTER TABLE productioncounting_productionbalance ADD COLUMN materialcostmarginvalue numeric(10,3);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN materialcostmarginvalue SET DEFAULT 0::numeric;

ALTER TABLE productioncounting_productionbalance ADD COLUMN additionaloverheadvalue numeric(10,3);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN additionaloverheadvalue SET DEFAULT 0::numeric;

ALTER TABLE productioncounting_productionbalance ADD COLUMN totalOverhead numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN totalcosts numeric(10,3);
ALTER TABLE productioncounting_productionbalance ADD COLUMN totalcostperunit numeric(10,3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN quantity numeric(10, 3);

ALTER TABLE productioncounting_productionbalance ADD COLUMN technology_id bigint;
ALTER TABLE productioncounting_productionbalance ADD CONSTRAINT productionbalance_technology_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE;
      
ALTER TABLE productioncounting_productionbalance ADD COLUMN generatedwithcosts boolean;
UPDATE productioncounting_productionbalance SET generatedwithcosts=false;

-- end

      
-- Table: productioncounting_balanceoperationproductincomponent
-- changed: 12.03.2012

CREATE TABLE productioncounting_balanceoperationproductincomponent
(
  id bigint NOT NULL,
  productionrecord_id bigint,
  productionbalance_id bigint,
  product_id bigint,
  usedquantity numeric(8,3),
  plannedquantity numeric(8,3),
  balance numeric(10,3),
  CONSTRAINT productioncounting_balanceoperationproductincomponent_pkey PRIMARY KEY (id),
  CONSTRAINT productioncounting_balanceoperationproductoutcomponent_p_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT productioncounting_balanceoperationproductincomponent_pb_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE,
  CONSTRAINT productioncounting_balanceoperationproductincomponent_pr_fkey FOREIGN KEY (productionrecord_id)
      REFERENCES productioncounting_productionrecord (id) DEFERRABLE
);

-- end


-- Table: productioncounting_balanceoperationproductoutcomponent
-- changed: 12.03.2012

CREATE TABLE productioncounting_balanceoperationproductoutcomponent
(
  id bigint NOT NULL,
  productionrecord_id bigint,
  productionbalance_id bigint,
  product_id bigint,
  usedquantity numeric(8,3),
  plannedquantity numeric(8,3),
  balance numeric(10,3),
  CONSTRAINT productioncounting_balanceoperationproductoutcomponent_pkey PRIMARY KEY (id),
  CONSTRAINT productioncounting_balanceoperationproductoutcomponent_p_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT productioncounting_balanceoperationproductoutcomponent_pb_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE,
  CONSTRAINT productioncounting_balanceoperationproductoutcomponent_pr_fkey FOREIGN KEY (productionrecord_id)
      REFERENCES productioncounting_productionrecord (id) DEFERRABLE
);

-- end


-- Table: productioncounting_operationtimecomponent
-- changed: 12.03.2012

CREATE TABLE productioncounting_operationtimecomponent
(
  id bigint NOT NULL,
  productionbalance_id bigint,
  orderoperationcomponent_id bigint,
  plannedmachinetime integer,
  machinetime integer,
  machinetimebalance integer,
  plannedlabortime integer,
  labortime integer,
  labortimebalance integer,
  CONSTRAINT productioncounting_operationtimecomponent_pkey PRIMARY KEY (id),
  CONSTRAINT productioncounting_operationtimecomponent_ooc_fkey FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) DEFERRABLE,
  CONSTRAINT productioncounting_operationtimecomponent_pb_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE
);

-- end


-- Table: productioncountingwithcosts_orderoperationproductincomponent
-- changed: 12.03.2012

CREATE TABLE productioncountingwithcosts_orderoperationproductincomponent
(
  id bigint NOT NULL,
  productionbalance_id bigint,
  product_id bigint,
  plannedcost numeric(10,3),
  registeredcost numeric(10,3),
  balance numeric(10,3),
  CONSTRAINT productioncountingwithcosts_orderoperationpic_pkey PRIMARY KEY (id),
  CONSTRAINT productioncountingwithcosts_orderoperationpic_p_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT productioncountingwithcosts_orderoperationpic_pb_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE
);

-- end


-- Table: productioncountingwithcosts_orderoperationproductincomponent
-- changed: 12.03.2012

CREATE TABLE productioncountingwithcosts_operationcostcomponent
(
  id bigint NOT NULL,
  productionbalance_id bigint,
  orderoperationcomponent_id bigint,
  plannedmachinecosts numeric(10,3),
  machinecosts numeric(10,3),
  machinecostsbalance numeric(10,3),
  plannedlaborcosts numeric(10,3),
  laborcosts numeric(10,3),
  laborcostsbalance numeric(10,3),
  CONSTRAINT productioncountingwithcosts_operationcostcomponent_pkey PRIMARY KEY (id),
  CONSTRAINT productioncountingwithcosts_operationcostcomponent_orc_fkey FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) DEFERRABLE,
  CONSTRAINT productioncountingwithcosts_operationcostcomponent_pb_fkey FOREIGN KEY (productionbalance_id)
      REFERENCES productioncounting_productionbalance (id) DEFERRABLE
);

-- end





-- Table: workplans_columnfororders
-- changed: 15.03.2012

CREATE TABLE workplans_columnfororders
(
  id bigint NOT NULL,
  identifier character varying(255),
  "name" character varying(1024),
  description character varying(255),
  columnfiller character varying(2048),
  CONSTRAINT workplans_columnfororders_pkey PRIMARY KEY (id)
);

-- end


-- Table: workplans_parameterordercolumn
-- changed: 15.03.2012

CREATE TABLE workplans_parameterordercolumn
(
  id bigint NOT NULL,
  parameter_id bigint,
  columnfororders_id bigint,
  succession integer,
  CONSTRAINT workplans_parameterordercolumn_pkey PRIMARY KEY (id),
  CONSTRAINT workplans_parameterordercolumn_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) DEFERRABLE,
  CONSTRAINT workplans_parameterordercolumn_columnfororders_fkey FOREIGN KEY (columnfororders_id)
      REFERENCES workplans_columnfororders (id) DEFERRABLE
);

-- end


-- Table: workplans_workplanordercolumn
-- changed: 15.03.2012

CREATE TABLE workplans_workplanordercolumn
(
  id bigint NOT NULL,
  workplan_id bigint,
  columnfororders_id bigint,
  succession integer,
  CONSTRAINT workplans_workplanordercolumn_pkey PRIMARY KEY (id),
  CONSTRAINT workplans_workplanordercolumn_workplan_fkey FOREIGN KEY (workplan_id)
      REFERENCES workplans_workplan (id) DEFERRABLE,
  CONSTRAINT workplans_workplanordercolumn_columnfororders_fkey FOREIGN KEY (columnfororders_id)
      REFERENCES workplans_columnfororders (id) DEFERRABLE
);

-- end


-- Table: basic_parameter
-- changed: 19.03.2012

ALTER TABLE basic_parameter ADD COLUMN dontPrintOrdersInWorkPlans boolean;

-- end


-- Table: workplans_workplan
-- changed: 19.03.2012

ALTER TABLE workplans_workplan ADD COLUMN dontPrintOrdersInWorkPlans boolean;

-- end


-- Table: productioncounting_productionbalance
-- changed: 23.03.2012

UPDATE productioncounting_productionbalance SET calculateoperationcostsmode='01hourly';

-- end