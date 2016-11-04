-- #ANEKS-48
ALTER TABLE basic_parameter ADD ppsIsAutomatic boolean;
ALTER TABLE basic_parameter ADD ppsProducedAmountRecalculatePlan boolean;
ALTER TABLE basic_parameter ADD ppsAlgorithm character varying(255);
-- end

-- #QCADOOCLS-5097
ALTER TABLE basic_company ALTER COLUMN website TYPE character varying(255);
-- end
