
----- WORKPLANS ----

-- Table: basic_division

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
)
WITH (
  OIDS=FALSE
);

ALTER TABLE basic_machine RENAME TO basic_workstationType;

ALTER TABLE basic_workstationType ADD COLUMN division_id bigint;

ALTER TABLE basic_workstationType ADD CONSTRAINT basic_workstationType_fkey_divisions  FOREIGN KEY (division_id)
	REFERENCES basic_division (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE technologies_operation DROP COLUMN staff_id;


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
)
WITH (OIDS=FALSE);

BEGIN;
	DELETE FROM workplans_workplancomponent;
	DELETE FROM workplans_workplan;
COMMIT;

DROP TABLE workplans_workplancomponent;

ALTER TABLE workplans_workplan ADD COLUMN type character varying(255) DEFAULT '01allOperations'::character varying;

alter table basic_parameter add column showDescriptionInWorkPlans boolean;
alter table basic_parameter add column showDetailsInWorkPlans boolean;
alter table basic_parameter add column imageUrlInWorkPlan character varying(255);
alter table basic_parameter add column dontPrintInputProductsInWorkPlans boolean;
alter table basic_parameter add column dontPrintOutputProductsInWorkPlans boolean;