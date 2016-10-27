-- #ANEKS-48
ALTER TABLE basic_parameter ADD ppsIsAutomatic boolean;
ALTER TABLE basic_parameter ADD ppsProducedAmountRecalculatePlan boolean;
ALTER TABLE basic_parameter ADD ppsAlgorithm character varying(255);
-- end

