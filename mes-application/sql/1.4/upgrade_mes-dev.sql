-- Columns needed by OzgoHome
-- last touched 19.09.2014 by kama

ALTER TABLE materialflowresources_position ADD COLUMN resource_id bigint;
ALTER TABLE materialflowresources_position ADD CONSTRAINT position_resource_fkey FOREIGN KEY (resource_id)
      REFERENCES materialflowresources_resource (id) DEFERRABLE;

-- end

-- Added resource related columns in deliveredProduct
-- last touched 19.09.2014 by kama

ALTER TABLE deliveries_deliveredproduct ADD COLUMN batch character varying(255);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN productiondate date;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN expirationdate date;

-- end

-- Added update scripts for workPlans translations
-- last touched 22.10.2014 by kama

UPDATE workplans_columnfororders
   SET identifier='numberOrderColumn', name='workPlans.columnForOrders.name.value.numberOrderColumn', description='workPlans.columnForOrders.description.value.numberOrderColumn'
   WHERE identifier='orderNumber';

UPDATE workplans_columnfororders
   SET identifier='nameOrderColumn', name='workPlans.columnForOrders.name.value.nameOrderColumn', description='workPlans.columnForOrders.description.value.nameOrderColumn'
   WHERE identifier='orderName';

UPDATE workplans_columnfororders
   SET identifier='productNameOrderColumn', name='workPlans.columnForOrders.name.value.productNameOrderColumn', description='workPlans.columnForOrders.description.value.productNameOrderColumn'
   WHERE identifier='productName';

UPDATE workplans_columnfororders
   SET identifier='plannedQuantityOrderColumn', name='workPlans.columnForOrders.name.value.plannedQuantityOrderColumn', description='workPlans.columnForOrders.description.value.plannedQuantityOrderColumn'
   WHERE identifier='plannedQuantity';

UPDATE workplans_columnfororders
   SET identifier='plannedEndDateOrderColumn', name='workPlans.columnForOrders.name.value.plannedEndDateOrderColumn', description='workPlans.columnForOrders.description.value.plannedEndDateOrderColumn'
   WHERE identifier='plannedEndDate';

UPDATE workplans_columnfororders
   SET identifier='producedBatchesOrderColumn', name='workPlans.columnForOrders.name.value.producedBatches', description='workPlans.columnForOrders.description.value.producedBatches'
   WHERE identifier='producedBatches';

UPDATE workplans_columnforinputproducts
   SET identifier='productNameOperationProductColumn', name='workPlans.columnForInputProducts.name.value.productNameOperationProductColumn', description='workPlans.columnForInputProducts.description.value.productNameOperationProductColumn'
 WHERE identifier='productName';

UPDATE workplans_columnforinputproducts
   SET identifier='plannedQuantityOperationProductColumn', name='workPlans.columnForInputProducts.name.value.plannedQuantityOperationProductColumn', description='workPlans.columnForInputProducts.description.value.plannedQuantityOperationProductColumn'
 WHERE identifier='plannedQuantity';
 
UPDATE workplans_columnforinputproducts
   SET identifier='effectiveQuantityOperationProductColumn', name='workPlans.columnForInputProducts.name.value.effectiveQuantityOperationProductColumn', description='workPlans.columnForInputProducts.description.value.effectiveQuantityOperationProductColumn'
 WHERE identifier='effectiveQuantity';
 
UPDATE workplans_columnforinputproducts
   SET identifier='attentionOperationProductColumn', name='workPlans.columnForInputProducts.name.value.attentionOperationProductColumn', description='workPlans.columnForInputProducts.description.value.attentionOperationProductColumn'
 WHERE identifier='attention';
 
UPDATE workplans_columnforinputproducts
   SET identifier='employeeSignatureOperationProductColumn', name='workPlans.columnForInputProducts.name.value.employeeSignatureOperationProductColumn', description='workPlans.columnForInputProducts.description.value.employeeSignatureOperationProductColumn'
 WHERE identifier='employeeSignature';
 
UPDATE workplans_columnforinputproducts
   SET identifier='batchNumbersOperationProductColumn', name='workPlans.columnForInputProducts.name.value.batchNumbers', description='workPlans.columnForInputProducts.description.value.batchNumbers'
 WHERE identifier='batchNumbers';

 UPDATE workplans_columnforoutputproducts
   SET identifier='productNameOperationProductColumn', name='workPlans.columnForOutputProducts.name.value.productNameOperationProductColumn', description='workPlans.columnForOutputProducts.description.value.productNameOperationProductColumn'
 WHERE identifier='productName';

 UPDATE workplans_columnforoutputproducts
   SET identifier='plannedQuantityOperationProductColumn', name='workPlans.columnForOutputProducts.name.value.plannedQuantityOperationProductColumn', description='workPlans.columnForOutputProducts.description.value.plannedQuantityOperationProductColumn'
 WHERE identifier='plannedQuantity';
 
 UPDATE workplans_columnforoutputproducts
   SET identifier='effectiveQuantityOperationProductColumn', name='workPlans.columnForOutputProducts.name.value.effectiveQuantityOperationProductColumn', description='workPlans.columnForOutputProducts.description.value.effectiveQuantityOperationProductColumn'
 WHERE identifier='effectiveQuantity';
 
 UPDATE workplans_columnforoutputproducts
   SET identifier='attentionOperationProductColumn', name='workPlans.columnForOutputProducts.name.value.attentionOperationProductColumn', description='workPlans.columnForOutputProducts.description.value.attentionOperationProductColumn'
 WHERE identifier='attention';
 
 UPDATE workplans_columnforoutputproducts
   SET identifier='employeeSignatureOperationProductColumn', name='workPlans.columnForOutputProducts.name.value.employeeSignatureOperationProductColumn', description='workPlans.columnForOutputProducts.description.value.employeeSignatureOperationProductColumn'
 WHERE identifier='employeeSignature';

 -- end