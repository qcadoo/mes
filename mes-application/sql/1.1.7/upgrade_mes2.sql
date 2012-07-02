ALTER TABLE basic_substituteComponent ALTER COLUMN quantity TYPE numeric(9,5);

ALTER TABLE basicproductioncounting_basicproductioncounting ALTER COLUMN plannedquantity TYPE numeric(12,5);
ALTER TABLE basicproductioncounting_basicproductioncounting ALTER COLUMN usedquantity TYPE numeric(12,5);
ALTER TABLE basicproductioncounting_basicproductioncounting ALTER COLUMN usedquantity TYPE numeric(12,5);

ALTER TABLE costcalculation_costcalculation ALTER COLUMN quantity TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalmaterialcosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalmachinehourlycosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalpieceworkcosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totallaborhourlycosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totaltechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN productioncostmargin TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN productioncostmarginvalue TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN materialcostmargin TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN materialcostmarginvalue TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN additionaloverhead TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN additionaloverheadvalue TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totaloverhead TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalcosts TYPE numeric(12,5);
ALTER TABLE costcalculation_costcalculation ALTER COLUMN totalcostperunit TYPE numeric(12,5);

ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN costfornumber TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN nominalcost TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN lastpurchasecost TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN averagecost TYPE numeric(12,5);
ALTER TABLE costnormsformaterials_technologyinstoperproductincomp ALTER COLUMN costfororder TYPE numeric(12,5);

ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN machineutilization TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN laborutilization TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN pieces TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN operationcost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN operationmargincost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN totaloperationcost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE costnormsforoperation_calculationoperationcomponent ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE materialflow_stockcorrection ALTER COLUMN found TYPE numeric(12,5);
ALTER TABLE materialflow_transfer ALTER COLUMN quantity TYPE numeric(12,5);

ALTER TABLE orders_order ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE orders_order ALTER COLUMN donequantity TYPE numeric(10,5);

ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductincomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_balanceoperationproductoutcomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncounting_operationpieceworkcomponent ALTER COLUMN plannedcycles TYPE numeric(12,5);
ALTER TABLE productioncounting_operationpieceworkcomponent ALTER COLUMN cycles TYPE numeric(12,5);
ALTER TABLE productioncounting_operationpieceworkcomponent ALTER COLUMN cyclesbalance TYPE numeric(12,5);

ALTER TABLE productioncounting_productionrecord ALTER COLUMN executedoperationcycles TYPE numeric(12,5);

ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductincomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN plannedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN usedquantity TYPE numeric(10,5);
ALTER TABLE productioncounting_recordoperationproductoutcomponent ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN plannedmachinecosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN machinecosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN machinecostsbalance TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN plannedlaborcosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN laborcosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationcostcomponent ALTER COLUMN laborcostsbalance TYPE numeric(12,5);

ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN plannedcyclescosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN cyclescosts TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ALTER COLUMN cyclescostsbalance TYPE numeric(12,5);

ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN plannedcost TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN registeredcost TYPE numeric(12,5);
ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp ALTER COLUMN balance TYPE numeric(12,5);

ALTER TABLE productionpershift_dailyprogress ALTER COLUMN quantity TYPE numeric(12,5);

ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN controlledquantity TYPE numeric(12,5);
ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN takenforcontrolquantity TYPE numeric(12,5);
ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN rejectedquantity TYPE numeric(12,5);
ALTER TABLE qualitycontrols_qualitycontrol ALTER COLUMN accepteddefectsquantity TYPE numeric(12,5);

ALTER TABLE technologies_operationproductincomponent ALTER COLUMN quantity TYPE numeric(10,5);

ALTER TABLE technologies_operationproductoutcomponent ALTER COLUMN quantity TYPE numeric(10,5);

-- Extension model
ALTER TABLE technologies_operation ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN pieceworkcost TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN laborhourlycost TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN machinehourlycost TYPE numeric(12,5);

ALTER TABLE basic_product ALTER COLUMN costfornumber TYPE numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN nominalcost TYPE numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN lastpurchasecost TYPE numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN averagecost TYPE numeric(12,5);

ALTER TABLE technologies_technology ALTER COLUMN minimalquantity TYPE numeric(10,5);

ALTER TABLE productioncounting_productionbalance ALTER COLUMN averagemachinehourlycost TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN averagelaborhourlycost TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN productioncostmargin TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN materialcostmargin TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN additionaloverhead TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedcomponentscosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN componentscosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN componentscostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedmachinecosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN machinecosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN machinecostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedlaborcosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN laborcosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN laborcostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN plannedcyclescosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN cyclescosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN cyclescostsbalance TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN registeredtotaltechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN registeredtotaltechnicalproductioncostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totaltechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totaltechnicalproductioncostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN balancetechnicalproductioncosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN balancetechnicalproductioncostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN productioncostmarginvalue TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN materialcostmarginvalue TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN additionaloverheadvalue TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totaloverhead TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totalcosts TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN totalcostperunit TYPE numeric(12,5);
ALTER TABLE productioncounting_productionbalance ALTER COLUMN quantity TYPE numeric(12,5);

ALTER TABLE technologies_technology ALTER COLUMN unitsamplingnr TYPE numeric(12,5);

ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN machineutilization TYPE numeric(8,5);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN laborutilization TYPE numeric(8,5);

ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN machineutilization TYPE numeric(8,5);
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN laborutilization TYPE numeric(8,5);

ALTER TABLE technologies_operation ALTER COLUMN productioninonecycle TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN countmachine TYPE numeric(12,5);
ALTER TABLE technologies_operation ALTER COLUMN machineutilization TYPE numeric(8,5);
ALTER TABLE technologies_operation ALTER COLUMN laborutilization TYPE numeric(8,5);

