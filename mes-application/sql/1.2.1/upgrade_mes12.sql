-- Table: basic_parameter
-- changed: 12.05.2014

ALTER TABLE basic_parameter ADD COLUMN validateproductionrecordtimes boolean;

-- end

-- Table: productioncounting_productionrecord
-- changed: 12.05.2014

ALTER TABLE productioncounting_productionrecord ALTER COLUMN timerangefrom TYPE timestamp without time zone;

ALTER TABLE productioncounting_productionrecord ALTER COLUMN timerangeto TYPE timestamp without time zone;
-- end