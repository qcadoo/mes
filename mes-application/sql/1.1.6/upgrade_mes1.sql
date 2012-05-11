-- Table: productioncounting_productionrecord
-- changed: 27.04.2012
      
ALTER TABLE productioncounting_productionrecord DROP COLUMN plannedmachinetime;
ALTER TABLE productioncounting_productionrecord DROP COLUMN machinetimebalance;
ALTER TABLE productioncounting_productionrecord DROP COLUMN plannedlabortime;
ALTER TABLE productioncounting_productionrecord DROP COLUMN labortimebalance;
ALTER TABLE productioncounting_productionrecord DROP COLUMN plannedtime;

-- end

-- Table: basic_parameter
-- changed: 10.05.2012

BEGIN;
	ALTER TABLE basic_parameter ADD COLUMN sampleswereloaded BOOLEAN DEFAULT false;
	UPDATE basic_parameter SET sampleswereloaded = true;
COMMIT;

-- end