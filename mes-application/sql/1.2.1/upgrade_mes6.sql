
-- Plugin: masterorders
-- Table: masterorders_masterorder, masterorderproduct 
-- changed: 26.09.2013
-- author: kasi

ALTER TABLE masterorders_masterorder ALTER COLUMN masterorderquantity TYPE numeric(14, 5);
ALTER TABLE masterorders_masterorderproduct ALTER COLUMN masterorderquantity TYPE numeric(14, 5);
ALTER TABLE masterorders_masterorderproduct ALTER COLUMN cumulatedorderquantity TYPE numeric(14, 5);

-- end

-- Plugin: costcalculation
-- Table: costcalculation_costcalculation
-- changed: 26.09.2013
-- author: kasi

ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalmaterialcosts TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalmachinehourlycosts TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalpieceworkcosts TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totallaborhourlycosts TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totaltechnicalproductioncosts TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totaloverhead TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalcosts TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalcostperunit TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN additionaloverhead TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN productioncostmargin TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN productioncostmarginvalue TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN materialcostmargin TYPE numeric(19, 5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN materialcostmarginvalue TYPE numeric(19, 5);
-- end

-- Plugin: technologies
-- Table: technologies_operationproductincomponent, technologies_operationproductoutcomponent
-- changed: 26.09.2013
-- author: lupo

ALTER TABLE technologies_operationproductincomponent ALTER COLUMN quantity TYPE numeric(12, 5);

ALTER TABLE technologies_operationproductoutcomponent ALTER COLUMN quantity TYPE numeric(12, 5);

-- end


-- Plugin: orders
-- Table: orders_order
-- changed: 26.09.2013
-- author: lupo

ALTER TABLE orders_order ALTER COLUMN plannedquantity TYPE numeric(12, 5);
ALTER TABLE orders_order ALTER COLUMN donequantity TYPE numeric(12, 5);
ALTER TABLE orders_order ALTER COLUMN commissionedplannedquantity TYPE numeric(12, 5);
ALTER TABLE orders_order ALTER COLUMN commissionedcorrectedquantity TYPE numeric(12, 5);
ALTER TABLE orders_order ALTER COLUMN amountofproductproduced TYPE numeric(12, 5);
ALTER TABLE orders_order ALTER COLUMN remainingamountofproducttoproduce TYPE numeric(12, 5);

-- end


-- Plugin: basicproductioncounting
-- Table: basicproductioncounting_productioncountingquantity
-- changed: 26.09.2013
-- author: lupo

ALTER TABLE basicproductioncounting_productioncountingquantity ALTER COLUMN plannedquantity TYPE numeric(14, 5);

-- end


-- Plugin: productioncounting
-- Table: productioncounting_balanceoperationproductincomponent, productioncounting_balanceoperationproductoutcomponent, productioncounting_recordoperationproductincomponent, productioncounting_recordoperationproductoutcomponent
-- changed: 26.09.2013
-- author: lupo

ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN plannedquantity TYPE numeric(12, 5);
ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN usedquantity TYPE numeric(12, 5);
ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN balance TYPE numeric(12, 5);

ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN plannedquantity TYPE numeric(12, 5);
ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN usedquantity TYPE numeric(12, 5);
ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN balance TYPE numeric(12, 5);

ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN usedquantity TYPE numeric(12, 5);
ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN balance TYPE numeric(12, 5);

ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN usedquantity TYPE numeric(12, 5);
ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN balance TYPE numeric(12, 5);

-- end


-- Plugin: productioncountingwithcosts
-- Table: productioncountingwithcosts_productionbalance, productioncountingwithcosts_operationcostcomponent, productioncountingwithcosts_operationpieceworkcostcomponent, productioncountingwithcosts_technologyinstoperproductincomp
-- changed: 27.09.2013
-- author: lupo

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN averageMachineHourlyCost TYPE numeric(12, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN averageLaborHourlyCost TYPE numeric(12, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN productionCostMargin TYPE numeric(12, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN materialCostMargin TYPE numeric(12, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN additionalOverhead TYPE numeric(12, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN plannedComponentsCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN componentsCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN componentsCostsBalance TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN plannedMachineCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN machineCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN machineCostsBalance TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN plannedLaborCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN laborCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN laborCostsBalance TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN plannedCyclesCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN cyclesCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN cyclesCostsBalance TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN registeredTotalTechnicalProductionCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN registeredTotalTechnicalProductionCostPerUnit TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN totalTechnicalProductionCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN totalTechnicalProductionCostPerUnit TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN balanceTechnicalProductionCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN balanceTechnicalProductionCostPerUnit TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN productionCostMarginValue TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN materialCostMarginValue TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN additionalOverheadValue TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN totalOverhead TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN totalCosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN totalCostPerUnit TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_productionbalance ALTER COLUMN quantity TYPE numeric(12, 5);

ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN plannedmachinecosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN machinecosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN machinecostsbalance TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN plannedlaborcosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN laborcosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN laborcostsbalance TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN plannedcyclescosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN cyclescosts TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN cyclescostsbalance TYPE numeric(19, 5);

ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN plannedcost TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN registeredcost TYPE numeric(19, 5);
ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN balance TYPE numeric(19, 5);

-- end

