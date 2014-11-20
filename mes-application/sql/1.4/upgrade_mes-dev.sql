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


-- Add table for resource correction
-- last touched 26.09.2014 by kama

CREATE TABLE materialflowresources_resourcecorrection
(
  id bigint NOT NULL,
  "number" character varying(255),
  product_id bigint,
  newquantity numeric(12,5),
  oldquantity numeric(12,5),
  location_id bigint,
  "time" timestamp without time zone,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  batch character varying(255),
  resource_id bigint,
  CONSTRAINT materialflowresources_resourcecorrection_pkey PRIMARY KEY (id),
  CONSTRAINT resourcecorrection_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT resourcecorrection_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT resourcecorrection_resource_fkey FOREIGN KEY (resource_id)
      REFERENCES materialflowresources_resource (id) DEFERRABLE
);

-- end


-- Add tables for dynamic attributes
-- last touched 03.10.2014 by kama

CREATE TABLE materialflowresources_attribute
(
  id bigint NOT NULL,
  name character varying(255),
  location_id bigint,
  required boolean DEFAULT false,
  CONSTRAINT materialflowresources_attribute_pkey PRIMARY KEY (id),
  CONSTRAINT attribute_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

CREATE TABLE materialflowresources_attributevalue
(
  id bigint NOT NULL,
  attribute_id bigint,
  value character varying(255),
  position_id bigint,
  resource_id bigint,
  CONSTRAINT materialflowresources_attributevalue_pkey PRIMARY KEY (id),
  CONSTRAINT attributevalue_position_fkey FOREIGN KEY (position_id)
      REFERENCES materialflowresources_position (id) DEFERRABLE,
  CONSTRAINT attributevalue_resource_fkey FOREIGN KEY (resource_id)
      REFERENCES materialflowresources_resource (id) DEFERRABLE,
  CONSTRAINT attributevalue_attribute_fkey FOREIGN KEY (attribute_id)
      REFERENCES materialflowresources_attribute (id) DEFERRABLE
);

-- end


-- Added column in resources
-- last touched 03.10.2014 by kama

ALTER TABLE materialflowresources_resource ADD COLUMN iscorrected BOOLEAN;

-- end


-- Add operation column in transformations
-- last touched 22.10.2014 by kama

ALTER TABLE materialflow_transformations ADD COLUMN operation_id bigint;
ALTER TABLE materialflow_transformations ADD CONSTRAINT transformations_operation_fkey FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) DEFERRABLE;

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


-- Add parameters for additional rows in work workPlans
-- last touched 22.10.2014 by kama

ALTER TABLE basic_parameter ADD COLUMN additionaloutputrows integer;
ALTER TABLE basic_parameter ADD COLUMN additionalinputrows integer;

-- end


-- orders_order
-- last touched 04.11.2014 by kasi

ALTER TABLE orders_order ADD COLUMN masterorderproduct_id bigint;
ALTER TABLE orders_order
	ADD CONSTRAINT masterorderproduct_order_fkey FOREIGN KEY (masterorderproduct_id)
		REFERENCES basic_product (id);
-- end


-- materialflow_transformations
-- last touched 20.11.2014 by lupo

ALTER TABLE materialflow_transformations ALTER COLUMN time TYPE date;

-- end

