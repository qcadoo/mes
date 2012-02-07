-- Table: orders_order
-- changed: 26.01.2012

ALTER TABLE orders_order ADD COLUMN description varchar(2048);
ALTER TABLE orders_order ALTER COLUMN name type varchar(1024);
ALTER TABLE orders_order ALTER COLUMN "number" type varchar(255);

-- end


-- Table: basic_workstationtype
-- changed: 26.01.2012

ALTER TABLE basic_workstationtype ALTER COLUMN name type varchar(1024);

-- end


-- Table: basic_workstationtype
-- changed: 26.01.2012

ALTER TABLE basic_substitute ALTER COLUMN name type varchar(1024);
ALTER TABLE basic_substitute ALTER COLUMN "number" type varchar(255);

-- end


-- Table: basic_product
-- changed: 26.01.2012

ALTER TABLE basic_product ALTER COLUMN name type varchar(1024);
ALTER TABLE basic_product ALTER COLUMN "number" type varchar(255);

-- end


-- Table: basic_division
-- changed: 26.01.2012

ALTER TABLE basic_division ALTER COLUMN name type varchar(1024);
ALTER TABLE basic_division ALTER COLUMN "number" type varchar(255);

-- end


-- Table: basic_shift
-- changed: 26.01.2012

ALTER TABLE basic_shift ALTER COLUMN name type varchar(1024);

-- end


-- Table: basic_shifttimetableexception
-- changed: 26.01.2012

ALTER TABLE basic_shifttimetableexception ALTER COLUMN name type varchar(1024);

-- end


-- Table: costcalculation_costcalculation
-- changed: 26.01.2012

ALTER TABLE costcalculation_costcalculation ALTER COLUMN "number" type varchar(255);

-- end


-- Table: materialflow_materialsinstockareas
-- changed: 26.01.2012

ALTER TABLE materialflow_materialsinstockareas ALTER COLUMN name type varchar(1024);

-- end


-- Table: materialflow_stockareas
-- changed: 26.01.2012

ALTER TABLE materialflow_stockareas ALTER COLUMN "number" type varchar(255);

-- end


-- Table: materialflow_stockcorrection
-- changed: 26.01.2012

ALTER TABLE materialflow_stockcorrection ALTER COLUMN "number" type varchar(255);

-- end


-- Table: materialflow_transfer
-- changed: 26.01.2012

ALTER TABLE materialflow_transfer ALTER COLUMN "number" type varchar(255);

-- end


-- Table: materialflow_transfer
-- changed: 26.01.2012

ALTER TABLE materialflow_transformations ALTER COLUMN "number" type varchar(255);
ALTER TABLE materialflow_transformations ALTER COLUMN name type varchar(1024);

-- end


-- Table: materialrequirements_materialrequirement
-- changed: 26.01.2012

ALTER TABLE materialrequirements_materialrequirement ALTER COLUMN name type varchar(1024);

-- end


-- Table: productioncounting_productionrecord
-- changed: 26.01.2012

ALTER TABLE productioncounting_productionrecord ALTER COLUMN "number" type varchar(255);

-- end


-- Table: productioncounting_productioncounting
-- changed: 26.01.2012

ALTER TABLE productioncounting_productioncounting ALTER COLUMN name type varchar(1024);

-- end


-- Table: productioncounting_productionbalance
-- changed: 26.01.2012

ALTER TABLE productioncounting_productionbalance ALTER COLUMN name type varchar(1024);

-- end


-- Table: qualitycontrols_qualitycontrol
-- changed: 26.01.2012

ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN "number" type varchar(255);

-- end


-- Table: simplematerialbalance_simplematerialbalance
-- changed: 26.01.2012

ALTER TABLE simplematerialbalance_simplematerialbalance ALTER COLUMN name type varchar(1024);

-- end


-- Table: technologies_operation
-- changed: 26.01.2012

ALTER TABLE technologies_operation ALTER COLUMN name type varchar(1024);

-- end


-- Table: workplans_columnforoutputproducts
-- changed: 26.01.2012

ALTER TABLE workplans_columnforoutputproducts ALTER COLUMN name type varchar(1024);
ALTER TABLE workplans_columnforoutputproducts ALTER COLUMN description type varchar(2048);

-- end


-- Table: workplans_columnforinputproducts
-- changed: 26.01.2012

ALTER TABLE workplans_columnforinputproducts ALTER COLUMN name type varchar(1024);
ALTER TABLE workplans_columnforinputproducts ALTER COLUMN description type varchar(2048);

-- end


-- Table: workplans_workplan
-- changed: 26.01.2012

ALTER TABLE workplans_workplan ALTER COLUMN name type varchar(1024);

-- end


-- Table: ordergroups_ordergroup
-- changed: 26.01.2012

ALTER TABLE ordergroups_ordergroup ALTER COLUMN name type varchar(1024);
ALTER TABLE ordergroups_ordergroup ALTER COLUMN "number" type varchar(255);

-- end

      
-- Table: basic_staff
-- changed: 27.01.2012

ALTER TABLE basic_staff ADD COLUMN shift_id bigint;
ALTER TABLE basic_staff ADD CONSTRAINT basic_staff_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE;
ALTER TABLE basic_staff ADD COLUMN division_id bigint;
ALTER TABLE basic_staff ADD CONSTRAINT basic_staff_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE;

-- end


-- Table: productioncounting_productionrecord
-- changed: 27.01.2012

ALTER TABLE productioncounting_productionrecord ADD COLUMN staff_id bigint;
ALTER TABLE productioncounting_productionrecord ADD CONSTRAINT productioncounting_productionrecord_s_fkey FOREIGN KEY (staff_id)
      REFERENCES basic_staff (id) DEFERRABLE;
ALTER TABLE productioncounting_productionrecord ADD COLUMN division_id bigint;
ALTER TABLE productioncounting_productionrecord ADD CONSTRAINT productioncounting_productionrecord_d_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE;
ALTER TABLE productioncounting_productionrecord ADD COLUMN workstationtype_id bigint;
ALTER TABLE productioncounting_productionrecord ADD CONSTRAINT productioncounting_productionrecord_wt_fkey FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) DEFERRABLE;

-- end


-- Table: productioncounting_productionrecordlogging
-- changed: 27.01.2012
  
CREATE TABLE productioncounting_productionrecordlogging
(
  id bigint NOT NULL,
  productionrecord_id bigint,
  dateandtime timestamp without time zone,
  previousstate character varying(255),
  currentstate character varying(255),
  worker character varying(255),
  CONSTRAINT productioncounting_productionrecordlogging_pkey PRIMARY KEY (id),
  CONSTRAINT productioncounting_productionrecordlogging_pr_fkey FOREIGN KEY (productionrecord_id)
      REFERENCES productioncounting_productionrecord (id) DEFERRABLE
);

-- end
