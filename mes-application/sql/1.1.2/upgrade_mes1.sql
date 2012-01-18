-- Table: basic_division
-- changed: 20.12.2011

CREATE TABLE basic_division
(
  id bigint NOT NULL,
  "number" character varying(40),
  "name" character varying(255),
  supervisor_id bigint,
  CONSTRAINT basic_division_pkey PRIMARY KEY (id),
  CONSTRAINT basic_division_fkey_staff FOREIGN KEY (supervisor_id)
      REFERENCES basic_staff (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: basic_machine
-- changed: 20.12.2011

ALTER TABLE basic_machine RENAME TO basic_workstationType;

-- end


-- Table: basic_workstationType
-- changed: 20.12.2011

ALTER TABLE basic_workstationType ADD COLUMN division_id bigint;

ALTER TABLE basic_workstationType ADD CONSTRAINT basic_workstationType_fkey_divisions  FOREIGN KEY (division_id)
	REFERENCES basic_division (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

-- end

	
-- Table: technologies_operation
-- changed: 20.12.2011
	
ALTER TABLE technologies_operation DROP COLUMN staff_id;

-- end


-- Table: jointable_order_workplan
-- changed: 20.12.2011

CREATE TABLE jointable_order_workplan
(
  order_id bigint NOT NULL,
  workplan_id bigint NOT NULL,
  CONSTRAINT jointable_order_workplan_pkey PRIMARY KEY (workplan_id, order_id),
  CONSTRAINT jointable_order_workplan_fkey_workplan FOREIGN KEY (workplan_id)
      REFERENCES workplans_workplan (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT jointable_order_workplan_fkey_order FOREIGN KEY (order_id)
      REFERENCES orders_order (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


--

BEGIN;
	DELETE FROM workplans_workplancomponent;
	DELETE FROM workplans_workplan;
COMMIT;

-- end


-- Table: workplans_workplancomponent
-- changed: 20.12.2011

DROP TABLE workplans_workplancomponent;

-- end


-- Table: workplans_workplan
-- changed: 20.12.2011

ALTER TABLE workplans_workplan ADD COLUMN type character varying(255) DEFAULT '01allOperations'::character varying;

-- end


-- Table: technologies_operation 
-- changed: 02.01.2012

ALTER TABLE technologies_operation ADD COLUMN workstationtype_id bigint;

ALTER TABLE technologies_operation ADD CONSTRAINT technologies_operation_fkey_workstation FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
	  
-- end      


-- Table: basic_parameter
-- changed: 02.01.2012

ALTER TABLE basic_parameter ADD COLUMN showDescriptionInWorkPlans boolean;
ALTER TABLE basic_parameter ADD COLUMN showDetailsInWorkPlans boolean;
ALTER TABLE basic_parameter ADD COLUMN imageUrlInWorkPlan character varying(255);
ALTER TABLE basic_parameter ADD COLUMN dontPrintInputProductsInWorkPlans boolean;
ALTER TABLE basic_parameter ADD COLUMN dontPrintOutputProductsInWorkPlans boolean;

-- end


-- Table: basic_parameter
-- changed: 03.01.2012

ALTER TABLE basic_parameter RENAME COLUMN showDetailsInWorkPlans TO hideDetailsInWorkPlans;
ALTER TABLE basic_parameter RENAME COLUMN showDescriptionInWorkPlans TO hideDescriptionInWorkPlans;

ALTER TABLE basic_parameter ADD COLUMN hideTechnologyAndOrderInWorkPlans boolean;

-- end


-- Table: basic_workstationtype
-- changed: 03.01.2012

ALTER TABLE basic_workstationtype ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: basic_staff
-- changed: 03.01.2012

ALTER TABLE basic_staff ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: ordergroups_ordergroup
-- changed: 03.01.2012

ALTER TABLE ordergroups_ordergroup ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: technologies_operation
-- changed: 03.01.2012

ALTER TABLE technologies_operation ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: stoppage_stoppage
-- changed: 03.01.2012

ALTER TABLE stoppage_stoppage ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: productioncounting_productionrecord
-- changed: 03.01.2012

ALTER TABLE productioncounting_productionrecord ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: productioncounting_productioncounting
-- changed: 03.01.2012

ALTER TABLE productioncounting_productioncounting ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: workplans_workplan
-- changed: 03.01.2012

ALTER TABLE workplans_workplan ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: materialflow_materialsinstockareas
-- changed: 03.01.2012

ALTER TABLE materialflow_materialsinstockareas ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: technologies_operation
-- changed: 03.01.2012

ALTER TABLE technologies_operation ADD COLUMN hideDescriptionInWorkPlans boolean;
ALTER TABLE technologies_operation ADD COLUMN hideDetailsInWorkPlans boolean;
ALTER TABLE technologies_operation ADD COLUMN hideTechnologyAndOrderInWorkPlans boolean;
ALTER TABLE technologies_operation ADD COLUMN imageUrlInWorkPlan character varying(255);
ALTER TABLE technologies_operation ADD COLUMN dontPrintInputProductsInWorkPlans boolean;
ALTER TABLE technologies_operation ADD COLUMN dontPrintOutputProductsInWorkPlans boolean;

-- end


-- Table: technologies_technologyoperationcomponent
-- changed: 03.01.2012

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN hideDescriptionInWorkPlans boolean;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN hideDetailsInWorkPlans boolean;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN hideTechnologyAndOrderInWorkPlans boolean;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN imageUrlInWorkPlan character varying(255);
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN dontPrintInputProductsInWorkPlans boolean;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN dontPrintOutputProductsInWorkPlans boolean;

-- end


-- Table: productionscheduling_orderoperationcomponent
-- changed: 03.01.2012

ALTER TABLE productionscheduling_orderoperationcomponent ADD COLUMN hideDescriptionInWorkPlans boolean;
ALTER TABLE productionscheduling_orderoperationcomponent ADD COLUMN hideDetailsInWorkPlans boolean;
ALTER TABLE productionscheduling_orderoperationcomponent ADD COLUMN hideTechnologyAndOrderInWorkPlans boolean;
ALTER TABLE productionscheduling_orderoperationcomponent ADD COLUMN imageUrlInWorkPlan character varying(255);
ALTER TABLE productionscheduling_orderoperationcomponent ADD COLUMN dontPrintInputProductsInWorkPlans boolean;
ALTER TABLE productionscheduling_orderoperationcomponent ADD COLUMN dontPrintOutputProductsInWorkPlans boolean;

-- end


-- Table: basic_product
-- changed: 03.01.2012

ALTER TABLE basic_product RENAME COLUMN typeofmaterial TO globalTypeOfMaterial;

-- end


-- Table: materialrequirements_materialrequirement
-- changed: 05.01.2012

ALTER TABLE materialrequirements_materialrequirement ADD COLUMN active boolean DEFAULT true;

-- end


-- Table: qcadooview_view
-- changed: 05.01.2012

UPDATE qcadooview_view SET name='workstationTypesList', view='workstationTypesList' WHERE name='machinesList';

-- end


-- Table: qcadooview_item
-- changed: 05.01.2012

UPDATE qcadooview_item SET name='workstationTypes' WHERE name='machines';

-- end


-- Table: basic_company

-- changed: 11.01.2012
	
ALTER TABLE basic_company ADD COLUMN externalnumber varchar(255);


INSERT INTO basic_company SELECT * FROM basic_contractor;

-- end


-- Table: orders_order
-- changed: 16.01.2012

ALTER TABLE orders_order DROP CONSTRAINT fk3daecd74aea6e4cc;

-- end


-- Table: basic_contractor
-- changed: 11.01.2012

DROP TABLE basic_contractor;

-- end


-- Table: orders_order
-- changed: 12.01.2012

ALTER TABLE orders_order RENAME COLUMN contractor_id TO company_id;
ALTER TABLE orders_order ADD CONSTRAINT company_company_fkey FOREIGN KEY (company_id) REFERENCES basic_company (id);

-- end


-- Table: workplans_columnforoutputproducts
-- changed: 18.01.2012

CREATE TABLE workplans_columnforinputproducts
(
  id bigint NOT NULL,
  name character varying(255),
  description character varying(255),
  columnfiller character varying(255),
  CONSTRAINT workplans_columnforinputproducts_pkey PRIMARY KEY (id )
);

-- end


-- Table: workplans_columnforoutputproducts
-- changed: 18.01.2012

CREATE TABLE workplans_columnforoutputproducts
(
  id bigint NOT NULL,
  name character varying(255),
  description character varying(255),
  columnfiller character varying(255),
  CONSTRAINT workplans_columnforoutputproducts_pkey PRIMARY KEY (id )
);

-- end


-- Table: workplans_parameterinputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_parameterinputcolumn
(
  id bigint NOT NULL,
  parameter_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_parameterinputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_parameteroutputcolumn_parameter_fkey  FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_parameterinputcolumn_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_parameteroutputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_parameteroutputcolumn
(
  id bigint NOT NULL,
  parameter_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_parameteroutputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_parameteroutputcolumn_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_parameteroutputcolumn_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_operationinputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_operationinputcolumn
(
  id bigint NOT NULL,
  operation_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_operationinputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_operationinputcolumn_operation_fkey  FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_operationinputcolumn_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_operationoutputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_operationoutputcolumn
(
  id bigint NOT NULL,
  operation_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_operationoutputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_operationoutputcolumn_operation_fkey FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_operationoutputcolumn_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_technologyoperationinputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_technologyoperationinputcolumn
(
  id bigint NOT NULL,
  technologyoperationcomponent_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_technologyoperationinputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_technologyoperationinputcolumn_toc_fkey  FOREIGN KEY (technologyoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_technologyoperationinputcolumn_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_technologyoperationoutputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_technologyoperationoutputcolumn
(
  id bigint NOT NULL,
  technologyoperationcomponent_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_technologyoperationoutputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_technologyoperationoutputcolumn_toc_fkey FOREIGN KEY (technologyoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_technologyoperationoutputcolumn_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_orderoperationinputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_orderoperationinputcolumn
(
  id bigint NOT NULL,
  orderoperationcomponent_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_orderoperationinputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_orderoperationinputcolumn_ooc_fkey  FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_orderoperationinputcolumn_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_orderoperationoutputcolumn
-- changed: 18.01.2012

CREATE TABLE workplans_orderoperationoutputcolumn
(
  id bigint NOT NULL,
  orderoperationcomponent_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_orderoperationoutputcolumn_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_orderoperationoutputcolumn_ooc_fkey FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_orderoperationoutputcolumn_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: basic_parameter
-- changed: 18.01.2012

UPDATE basic_parameter SET 
	hideDescriptionInWorkPlans = false,
	hideDetailsInWorkPlans = false,
	hideTechnologyAndOrderInWorkPlans = false,
	dontPrintInputProductsInWorkPlans = false,
	dontPrintOutputProductsInWorkPlans = false;
	
-- end


-- Table: technologies_operation
-- changed: 18.01.2012

UPDATE technologies_operation SET 
	hideDescriptionInWorkPlans = false,
	hideDetailsInWorkPlans = false,
	hideTechnologyAndOrderInWorkPlans = false,
	dontPrintInputProductsInWorkPlans = false,
	dontPrintOutputProductsInWorkPlans = false;
	
-- end

-- Table: technologies_technologyoperationcomponent
-- changed: 18.01.2012

UPDATE technologies_technologyoperationcomponent SET 
	hideDescriptionInWorkPlans = false,
	hideDetailsInWorkPlans = false,
	hideTechnologyAndOrderInWorkPlans = false,
	dontPrintInputProductsInWorkPlans = false,
	dontPrintOutputProductsInWorkPlans = false;
	
-- end


-- Table: productionscheduling_orderoperationcomponent
-- changed: 18.01.2012

UPDATE productionscheduling_orderoperationcomponent SET 
	hideDescriptionInWorkPlans = false,
	hideDetailsInWorkPlans = false,
	hideTechnologyAndOrderInWorkPlans = false,
	dontPrintInputProductsInWorkPlans = false,
	dontPrintOutputProductsInWorkPlans = false;
	
-- end
