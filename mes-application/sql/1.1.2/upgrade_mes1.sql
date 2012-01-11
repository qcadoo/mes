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
  CONSTRAINT workplans_parameteroutputcomponent_columnforinputproducts_fkey FOREIGN KEY (columnforinputproducts_id)
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
  CONSTRAINT workplans_parameteroutputcomponent_columnforoutputproducts_fkey FOREIGN KEY (columnforoutputproducts_id)
      REFERENCES workplans_columnforoutputproducts (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: workplans_columnforinputproducts
-- changed: 11.01.2012

INSERT INTO workplans_columnforinputproducts (id, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'productName', 'productName', 'workPlans');
INSERT INTO workplans_columnforinputproducts (id, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'plannedQuantity', 'plannedQuantity', 'workPlans');

-- end


-- Table: workplans_columnforoutputproducts
-- changed: 11.01.2012

INSERT INTO workplans_columnforoutputproducts (id, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'productName', 'productName', 'workPlans');
INSERT INTO workplans_columnforoutputproducts (id, name, description, pluginidentifier) VALUES (nextval('hibernate_sequence'), 'plannedQuantity', 'plannedQuantity', 'workPlans');

-- end


-- Table: workplans_parameterinputcomponent
-- changed: 11.01.2012

CREATE OR REPLACE FUNCTION update_parameterinputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		succession INTEGER;
		parameter RECORD;
		columnforinputproducts RECORD;  
	
	BEGIN  
		FOR parameter IN SELECT id FROM basic_parameter LOOP
			succession := 1;

			FOR columnforinputproducts IN SELECT id FROM workplans_columnforinputproducts LOOP  
				INSERT INTO workplans_parameterinputcomponent (id, parameter_id, columnforinputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					parameter."id",
					columnforinputproducts."id",
					succession
				);

				succession := succession + 1;
			END LOOP;

		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_parameterinputcomponent();

-- end


-- Table: workplans_parameteroutputcomponent
-- changed: 11.01.2012

CREATE OR REPLACE FUNCTION update_parameteroutputcomponent() RETURNS INTEGER AS 
'
	DECLARE
		succession INTEGER;
		parameter RECORD;
		columnforoutputproducts RECORD;  
	
	BEGIN  
		FOR parameter IN SELECT id FROM basic_parameter LOOP
			succession := 1;
			FOR columnforoutputproducts IN SELECT id FROM workplans_columnforoutputproducts LOOP  
				INSERT INTO workplans_parameteroutputcomponent (id, parameter_id, columnforoutputproducts_id, succession) 
				VALUES (
					nextval(''hibernate_sequence''),  
					parameter."id",
					columnforoutputproducts."id",
					succession
				);

				succession := succession + 1;
			END LOOP;
		END LOOP;

		RETURN 1; 
	END;  
' 
LANGUAGE 'plpgsql';
SELECT * FROM update_parameteroutputcomponent();

-- end
