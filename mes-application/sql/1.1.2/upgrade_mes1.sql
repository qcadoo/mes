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


-- Table: workplans_columnforoutputproducts
-- changed: 11.01.2012

CREATE TABLE workplans_columnforinputproducts
(
  id bigint NOT NULL,
  name character varying(255),
  description character varying(255),
  pluginidentifier character varying(255),
  CONSTRAINT workplans_columnforinputproducts_pkey PRIMARY KEY (id )
);

-- end


-- Table: workplans_columnforoutputproducts
-- changed: 11.01.2012

CREATE TABLE workplans_columnforoutputproducts
(
  id bigint NOT NULL,
  name character varying(255),
  description character varying(255),
  pluginidentifier character varying(255),
  CONSTRAINT workplans_columnforoutputproducts_pkey PRIMARY KEY (id )
);

-- end


-- Table: workplans_parameterinputcomponent
-- changed: 11.01.2012

CREATE TABLE workplans_parameterinputcomponent
(
  id bigint NOT NULL,
  parameter_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_parameterinputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_parameteroutputcomponent_parameter_fkey  FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_parameterinputcomponent_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_parameteroutputcomponent
-- changed: 11.01.2012

CREATE TABLE workplans_parameteroutputcomponent
(
  id bigint NOT NULL,
  parameter_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_parameteroutputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_parameteroutputcomponent_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_parameteroutputcomponent_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: basic_company

-- changed: 11.01.2012
	
ALTER TABLE basic_company ADD COLUMN externalnumber varchar(255);


INSERT INTO basic_company SELECT * FROM basic_contractor;

-- end


-- Table: basic_contractor
-- changed: 11.01.2012

DROP TABLE basic_contractor;


-- end


-- Table: basic_parameter
-- changed: 12.01.2012

UPDATE basic_parameter SET 
	hideDescriptionInWorkPlans = false,
	hideDetailsInWorkPlans = false,
	hideTechnologyAndOrderInWorkPlans = false,
	dontPrintInputProductsInWorkPlans = false,
	dontPrintOutputProductsInWorkPlans = false;
	
-- end


-- Table: orders_order
-- changed: 12.01.2012

ALTER TABLE orders_order DROP CONSTRAINT fk3daecd74aea6e4cc;
ALTER TABLE orders_order RENAME COLUMN contractor_id TO company_id;
ALTER TABLE orders_order ADD CONSTRAINT company_company_fkey FOREIGN KEY (company_id) REFERENCES basic_company (id);

-- end


-- Table: workplans_columndefinition
-- changed: 13.01.2012

CREATE TABLE workplans_columndefinition
(
  id bigint NOT NULL,
  identifier character varying(255),
  name character varying(255),
  description character varying(255),
  pluginidentifier character varying(255),
  CONSTRAINT workplans_columndefinition_pkey PRIMARY KEY (id )
);

-- end


-- Table: workplans_columnforinputproducts
-- changed: 13.01.2012

ALTER TABLE workplans_columnforinputproducts DROP COLUMN name; 
ALTER TABLE workplans_columnforinputproducts DROP COLUMN description; 
ALTER TABLE workplans_columnforinputproducts DROP COLUMN pluginidentifier; 

ALTER TABLE workplans_columnforinputproducts ADD COLUMN columndefinition_id bigint;

ALTER TABLE workplans_columnforinputproducts ADD CONSTRAINT workplans_columnforinputproducts_cd_fkey FOREIGN KEY (columndefinition_id)
      REFERENCES workplans_columndefinition (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
      
-- end


-- Table: workplans_columnforoutputproducts
-- changed: 13.01.2012

ALTER TABLE workplans_columnforoutputproducts DROP COLUMN name; 
ALTER TABLE workplans_columnforoutputproducts DROP COLUMN description; 
ALTER TABLE workplans_columnforoutputproducts DROP COLUMN pluginidentifier; 

ALTER TABLE workplans_columnforoutputproducts ADD COLUMN columndefinition_id bigint;

ALTER TABLE workplans_columnforoutputproducts ADD CONSTRAINT workplans_columnforoutputproducts_cd_fkey FOREIGN KEY (columndefinition_id)
      REFERENCES workplans_columndefinition (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

-- end


-- Table: workplans_operationinputcomponent
-- changed: 13.01.2012

CREATE TABLE workplans_operationinputcomponent
(
  id bigint NOT NULL,
  operation_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_operationinputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_operationinputcomponent_operation_fkey  FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_operationinputcomponent_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_operationoutputcomponent
-- changed: 13.01.2012

CREATE TABLE workplans_operationoutputcomponent
(
  id bigint NOT NULL,
  operation_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_operationoutputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_operationoutputcomponent_operation_fkey FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_operationoutputcomponent_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_technologyoperationinputcomponent
-- changed: 13.01.2012

CREATE TABLE workplans_technologyoperationinputcomponent
(
  id bigint NOT NULL,
  technologyoperationcomponent_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_technologyoperationinputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_technologyoperationinputcomponent_toc_fkey  FOREIGN KEY (technologyoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_technologyoperationinputcomponent_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_technologyoperationoutputcomponent
-- changed: 13.01.2012

CREATE TABLE workplans_technologyoperationoutputcomponent
(
  id bigint NOT NULL,
  technologyoperationcomponent_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_technologyoperationoutputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_technologyoperationoutputcomponent_toc_fkey FOREIGN KEY (technologyoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_technologyoperationoutputcomponent_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_orderoperationinputcomponent
-- changed: 13.01.2012

CREATE TABLE workplans_orderoperationinputcomponent
(
  id bigint NOT NULL,
  orderoperationcomponent_id bigint,
  columnforinputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_orderoperationinputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_orderoperationinputcomponent_ooc_fkey  FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_orderoperationinputcomponent_cfip_fkey FOREIGN KEY (columnforinputproducts_id)
      REFERENCES workplans_columnforinputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_orderoperationoutputcomponent
-- changed: 13.01.2012

CREATE TABLE workplans_orderoperationoutputcomponent
(
  id bigint NOT NULL,
  orderoperationcomponent_id bigint,
  columnforoutputproducts_id bigint,
  succession integer,
  CONSTRAINT workplans_orderoperationoutputcomponent_pkey PRIMARY KEY (id ),
  CONSTRAINT workplans_orderoperationoutputcomponent_ooc_fkey FOREIGN KEY (orderoperationcomponent_id)
      REFERENCES productionscheduling_orderoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT workplans_orderoperationoutputcomponent_cfop_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end

      
---- ONLY FOR LOCAL INSTANCE ----      
      
-- Table: workplans_...
-- changed: 13.01.2012

TRUNCATE workplans_parameterinputcomponent;
TRUNCATE workplans_parameteroutputcomponent;
TRUNCATE workplans_columnforinputproducts CASCADE;
TRUNCATE workplans_columnforoutputproducts CASCADE;

-- end


-- Table: workplans_columndefinition
-- changed: 13.01.2012

INSERT INTO workplans_columndefinition (id, identifier, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'productName','workPlans.columnDefinition.name.value.productName', 'workPlans.columnDefinition.description.value.productName', 'workPlans');
			
INSERT INTO workplans_columndefinition (id, identifier, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'plannedQuantity','workPlans.columnDefinition.name.value.plannedQuantity', 'workPlans.columnDefinition.description.value.plannedQuantity', 'workPlans');

-- end


-- Table: workplans_columnforinputproducts
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_columforinputproducts() RETURNS INTEGER AS 
'
	DECLARE
		columndefinition RECORD;
		
	BEGIN  
		FOR columndefinition IN SELECT * FROM workplans_columndefinition LOOP  
			INSERT INTO workplans_columnforinputproducts (id, columndefinition_id) 
			VALUES (
				nextval(''hibernate_sequence''), 
				columndefinition."id"
			);
		END LOOP;
		
		RETURN 1; 
	END;
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_columforinputproducts();

-- end


-- Table: workplans_columnforinputproducts
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_columforoutputproducts() RETURNS INTEGER AS 
'
	DECLARE
		columndefinition RECORD;
		
	BEGIN  
		FOR columndefinition IN SELECT * FROM workplans_columndefinition LOOP  
			INSERT INTO workplans_columnforoutputproducts (id, columndefinition_id) 
			VALUES (
				nextval(''hibernate_sequence''), 
				columndefinition."id"
			);
		END LOOP;
		
		RETURN 1; 
	END;
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_columforoutputproducts();

-- end

      
-- Table: workplans_parameterinputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_parameterinputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		parameter RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR parameter IN SELECT id FROM basic_parameter LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_parameterinputcomponent (id, parameter_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					parameter."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_parameterinputcomponent();

-- end


-- Table: workplans_parameteroutputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_parameteroutputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		parameter RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR parameter IN SELECT id FROM basic_parameter LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_parameteroutputcomponent (id, parameter_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					parameter."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_parameteroutputcomponent();

-- end


-- Table: workplans_operationinputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_operationinputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		operation RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR operation IN SELECT id FROM technologies_operation LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_operationinputcomponent (id, operation_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					operation."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_operationinputcomponent();

-- end


-- Table: workplans_operationoutputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_operationoutputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		operation RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR operation IN SELECT id FROM technologies_operation LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_operationoutputcomponent (id, operation_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					operation."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_operationoutputcomponent();

-- end


-- Table: workplans_technologyoperationinputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_technologyoperationinputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		technologyoperationcomponent RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR technologyoperationcomponent IN SELECT id FROM technologies_technologyoperationcomponent LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_technologyoperationinputcomponent (id, technologyoperationcomponent_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					technologyoperationcomponent."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_technologyoperationinputcomponent();

-- end


-- Table: workplans_technologyoperationoutputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_technologyoperationoutputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		technologyoperationcomponent RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR technologyoperationcomponent IN SELECT id FROM technologies_technologyoperationcomponent LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_technologyoperationoutputcomponent (id, technologyoperationcomponent_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					technologyoperationcomponent."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_technologyoperationoutputcomponent();

-- end


-- Table: workplans_orderoperationinputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_orderoperationinputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		orderoperationcomponent RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR orderoperationcomponent IN SELECT id FROM productionscheduling_orderoperationcomponent LOOP
			priority := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_orderoperationinputcomponent (id, orderoperationcomponent_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					orderoperationcomponent."id",
					columnforinputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_orderoperationinputcomponent();

-- end


-- Table: workplans_operationoutputcomponent
-- changed: 13.01.2012

CREATE OR REPLACE FUNCTION update_orderoperationoutputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		priority INTEGER;
		orderoperationcomponent RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR orderoperationcomponent IN SELECT id FROM productionscheduling_orderoperationcomponent LOOP
			priority := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_orderoperationoutputcomponent (id, orderoperationcomponent_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					orderoperationcomponent."id",
					columnforoutputproducts."id",
					priority
				);

				priority := priority + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_orderoperationoutputcomponent();

-- end

---- END ----