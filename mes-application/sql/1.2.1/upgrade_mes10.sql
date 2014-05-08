-- Table: workplans_workplan
-- changed: 14.02.2014

ALTER TABLE workplans_workplan ADD COLUMN ordersorting CHARACTER VARYING(255);
ALTER TABLE workplans_workplan ADD COLUMN inputproductcolumntosortby_id BIGINT;
ALTER TABLE workplans_workplan ADD CONSTRAINT workplans_workplan_fkey FOREIGN KEY (inputproductcolumntosortby_id)
	REFERENCES workplans_columnforinputproducts (id) DEFERRABLE;

-- end


-- Table: operationproductincomponent
-- changed: 17.02.2014 [maku]
-- fix for missing table: itemNumberInTheExplodedView in public.technologies_operationproductincomponent

ALTER TABLE technologies_operationproductincomponent ADD COLUMN itemnumberintheexplodedview CHARACTER VARYING(255);

-- end

