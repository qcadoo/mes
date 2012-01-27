-- Table: technologies_technology
-- changed: 20.01.2012

ALTER TABLE technologies_technology DROP COLUMN componentquantityalgorithm;
	
-- end

-- Table: orders_order
-- changed: 26.01.2012

alter table orders_order add column description varchar(2048);
alter table orders_order alter column name type varchar(1024);
alter table orders_order alter column "number" type varchar(255);

-- end

-- Table: basic_workstationtype
-- changed: 26.01.2012

alter table basic_workstationtype alter column name type varchar(1024);

-- end

-- Table: basic_workstationtype
-- changed: 26.01.2012

alter table basic_substitute alter column name type varchar(1024);
alter table basic_substitute alter column "number" type varchar(255);

-- end

-- Table: basic_product
-- changed: 26.01.2012

alter table basic_product alter column name type varchar(1024);
alter table basic_product alter column "number" type varchar(255);

-- end

-- Table: basic_division
-- changed: 26.01.2012

alter table basic_division alter column name type varchar(1024);
alter table basic_division alter column "number" type varchar(255);

-- end

-- Table: basic_shift
-- changed: 26.01.2012

alter table basic_shift alter column name type varchar(1024);

-- end

-- Table: basic_shifttimetableexception
-- changed: 26.01.2012

alter table basic_shifttimetableexception alter column name type varchar(1024);

-- end

-- Table: costcalculation_costcalculation
-- changed: 26.01.2012

alter table costcalculation_costcalculation alter column "number" type varchar(255);

-- end

-- Table: materialflow_materialsinstockareas
-- changed: 26.01.2012

alter table materialflow_materialsinstockareas alter column name type varchar(1024);

-- end

-- Table: materialflow_stockareas
-- changed: 26.01.2012

alter table materialflow_stockareas alter column "number" type varchar(255);

-- end

-- Table: materialflow_stockcorrection
-- changed: 26.01.2012

alter table materialflow_stockcorrection alter column "number" type varchar(255);

-- end

-- Table: materialflow_transfer
-- changed: 26.01.2012

alter table materialflow_transfer alter column "number" type varchar(255);

-- end

-- Table: materialflow_transfer
-- changed: 26.01.2012

alter table materialflow_transformations alter column "number" type varchar(255);
alter table materialflow_transformations alter column name type varchar(1024);

-- end

-- Table: materialrequirements_materialrequirement
-- changed: 26.01.2012

alter table materialrequirements_materialrequirement alter column name type varchar(1024);

-- end

-- Table: productioncounting_productionrecord
-- changed: 26.01.2012

alter table productioncounting_productionrecord alter column "number" type varchar(255);

-- end

-- Table: productioncounting_productioncounting
-- changed: 26.01.2012

alter table productioncounting_productioncounting alter column name type varchar(1024);

-- end

-- Table: productioncounting_productionbalance
-- changed: 26.01.2012

alter table productioncounting_productionbalance alter column name type varchar(1024);

-- end

-- Table: qualitycontrols_qualitycontrol
-- changed: 26.01.2012

alter table qualitycontrols_qualitycontrol alter column "number" type varchar(255);

-- end

-- Table: simplematerialbalance_simplematerialbalance
-- changed: 26.01.2012

alter table simplematerialbalance_simplematerialbalance alter column name type varchar(1024);

-- end

-- Table: technologies_operation
-- changed: 26.01.2012

alter table technologies_operation alter column name type varchar(1024);

-- end

-- Table: workplans_columnforoutputproducts
-- changed: 26.01.2012

alter table workplans_columnforoutputproducts alter column name type varchar(1024);
alter table workplans_columnforoutputproducts alter column description type varchar(2048);

-- end

-- Table: workplans_columnforinputproducts
-- changed: 26.01.2012

alter table workplans_columnforinputproducts alter column name type varchar(1024);
alter table workplans_columnforinputproducts alter column description type varchar(2048);

-- end

-- Table: workplans_workplan
-- changed 26.01.2012

alter table workplans_workplan alter column name type varchar(1024);

-- end

-- Table: ordergroups_ordergroup
-- changed 26.01.2012

alter table ordergroups_ordergroup alter column name type varchar(1024);
alter table ordergroups_ordergroup alter column "number" type varchar(255);

-- end