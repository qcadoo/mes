----- WORKPLANS ----

-- Table: basic_division
-- changed 20.12.2011

CREATE TABLE basic_division
(
  id bigint NOT NULL,
  "number" character varying(40),
  "name" character varying(255),
  supervisor_id bigint,
  CONSTRAINT basic_division_pkey PRIMARY KEY (id),
  CONSTRAINT fkf0b0619e64280dc0 FOREIGN KEY (supervisor_id)
      REFERENCES basic_staff (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- end


-- Table: basic_machine
-- changed 20.12.2011

ALTER TABLE basic_machine RENAME TO basic_workstationType;

-- end


-- Table: basic_workstationType
-- changed 20.12.2011

ALTER TABLE basic_workstationType ADD COLUMN division_id bigint;

ALTER TABLE basic_workstationType ADD CONSTRAINT basic_workstationType_fkey_divisions  FOREIGN KEY (division_id)
	REFERENCES basic_division (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

-- end

	
-- Table: technologies_operation
-- changed 20.12.2011
	
ALTER TABLE technologies_operation DROP COLUMN staff_id;

-- end


-- Table: jointable_order_workplan
-- changed 20.12.2011

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
-- changed 20.12.2011

DROP TABLE workplans_workplancomponent;

-- end


-- Table: workplans_workplan
-- changed 20.12.2011

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
