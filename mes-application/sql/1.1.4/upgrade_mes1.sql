-- PBC1
alter table orders_order add column registerpiecework boolean;
alter table basic_parameter add column registerpiecework boolean;
update basic_parameter set registerpiecework=true;
alter table productioncounting_productionrecord add column executedOperationCycles integer;

-- Table: orders_order
-- changed: 28.02.2012

UPDATE basic_parameter SET batchnumberuniqueness='02supplier' WHERE batchnumberuniqueness='02manufacturer';

-- end
