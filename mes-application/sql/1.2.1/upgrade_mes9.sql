-- Table: workplans_workplan
-- changed: 14.02.2014

ALTER TABLE workplans_workplan ADD COLUMN ordersorting character varying(255);
ALTER TABLE workplans_workplan ADD COLUMN inputproductcolumntosortby_id bigint;
ALTER TABLE workplans_workplan
 	ADD CONSTRAINT workplans_workplan_fkey FOREIGN KEY (inputproductcolumntosortby_id)
 	REFERENCES workplans_columnforinputproducts (id) DEFERRABLE;
-- end 