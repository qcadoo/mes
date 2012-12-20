-- Table: basic_product
-- changed: 23.11.2012

ALTER TABLE basic_product DROP COLUMN companyproduct_id;
ALTER TABLE basic_product DROP COLUMN companyproductfamily_id;

-- end


-- Table: technologies_operation
-- changed: 23.11.2012

ALTER TABLE technologies_operation DROP COLUMN company_id;

-- end


-- Table: technologies_operationgroup
-- changed: 23.11.2012

ALTER TABLE technologies_operationgroup DROP COLUMN companyoperationgroup_id;

-- end


-- Table: deliveries_companyproduct
-- changed: 23.11.2012

CREATE TABLE deliveries_companyproduct
(
  id bigint NOT NULL,
  company_id bigint,
  product_id bigint,
  CONSTRAINT deliveries_companyproduct_pkey PRIMARY KEY (id),
  CONSTRAINT companyproduct_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT companyproduct_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end


-- Table: deliveries_companyproductsfamily
-- changed: 23.11.2012

CREATE TABLE deliveries_companyproductsfamily
(
  id bigint NOT NULL,
  company_id bigint,
  product_id bigint,
  CONSTRAINT deliveries_companyproductsfamily_pkey PRIMARY KEY (id),
  CONSTRAINT companyproductsfamily_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT companyproductsfamily_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end


-- Table: jointable_company_operation
-- changed: 23.11.2012

CREATE TABLE jointable_company_operation
(
  operation_id bigint NOT NULL,
  company_id bigint NOT NULL,
  CONSTRAINT jointable_company_operation_pkey PRIMARY KEY (company_id, operation_id),
  CONSTRAINT company_operation_operation_fkey FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) DEFERRABLE,
  CONSTRAINT company_operation_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE
);

-- end


-- Table: jointable_company_operationgroup
-- changed: 23.11.2012

CREATE TABLE jointable_company_operationgroup
(
  operationgroup_id bigint NOT NULL,
  company_id bigint NOT NULL,
  CONSTRAINT jointable_company_operationgroup_pkey PRIMARY KEY (company_id, operationgroup_id),
  CONSTRAINT company_operationgroup_operationgroup_fkey FOREIGN KEY (operationgroup_id)
      REFERENCES technologies_operationgroup (id) DEFERRABLE,
  CONSTRAINT company_operationgroup_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE
);

-- end


-- Table: basic_parameter
-- changed: 23.11.2012

UPDATE basic_parameter SET inputproductsrequiredfortype = '01startOrder' WHERE inputproductsrequiredfortype IS null;

-- end


-- Table: basic_product
-- changed: 23.11.2012

UPDATE orders_order SET inputproductsrequiredfortype = '01startOrder' WHERE inputproductsrequiredfortype IS null;

-- end


-- Table: deliveries_columnfororders
-- changed: 19.12.2012

ALTER TABLE deliveries_columnfororders ADD COLUMN parameter_id bigint;

ALTER TABLE deliveries_columnfororders
  ADD CONSTRAINT columnfororders_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) DEFERRABLE;

ALTER TABLE deliveries_columnfororders ADD COLUMN succession integer;

-- end


-- Table: deliveries_columnfordeliveries
-- changed: 19.12.2012

ALTER TABLE deliveries_columnfordeliveries ADD COLUMN parameter_id bigint;

ALTER TABLE deliveries_columnfordeliveries
  ADD CONSTRAINT columnfordeliveries_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) DEFERRABLE;
	  
ALTER TABLE deliveries_columnfordeliveries ADD COLUMN succession integer;

-- end


-- Table: basic_parameter
-- changed: 06.12.2012

ALTER TABLE basic_parameter ADD COLUMN hideemptycolumnsfororders boolean;
ALTER TABLE basic_parameter ALTER COLUMN hideemptycolumnsfororders SET DEFAULT false;

-- end


-- Table: basic_parameter
-- changed: 14.12.2012

ALTER TABLE basic_parameter ADD COLUMN additionaltextinfooter character varying(256);

-- end


-- Table: basic_parameter
-- changed: 19.12.2012

ALTER TABLE basic_parameter ADD COLUMN defaultdescription character varying(2048);

-- end


-- Table: basic_parameter
-- changed: 19.12.2012

ALTER TABLE basic_parameter ADD COLUMN otheraddress character varying(2048);
ALTER TABLE basic_parameter ADD COLUMN defaultaddress character varying(255);
ALTER TABLE basic_parameter ALTER COLUMN defaultaddress SET DEFAULT '01companyAddress'::character varying;

ALTER TABLE basic_parameter ADD COLUMN location_id bigint;

ALTER TABLE basic_parameter
  ADD CONSTRAINT parameter_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE;

ALTER TABLE basic_parameter ADD COLUMN company_id bigint;
  
ALTER TABLE basic_parameter
  ADD CONSTRAINT parameter_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE;
	  
-- end


-- Table: deliveries_orderedproduct
-- changed: 19.12.2012

ALTER TABLE deliveries_orderedproduct ADD COLUMN productcatalognumber_id bigint;

ALTER TABLE deliveries_orderedproduct
  ADD CONSTRAINT orderedproduct_productcatalognumber_fkey FOREIGN KEY (productcatalognumber_id)
      REFERENCES productcatalognumbers_productcatalognumbers (id) DEFERRABLE;

ALTER TABLE deliveries_orderedproduct ADD COLUMN succession integer;

ALTER TABLE deliveries_orderedproduct ADD COLUMN description character varying(2048);

-- end


-- Table: deliveries_deliveredproduct
-- changed: 19.12.2012

ALTER TABLE deliveries_deliveredproduct ADD COLUMN productcatalognumber_id bigint;

ALTER TABLE deliveries_deliveredproduct
  ADD CONSTRAINT orderedproduct_productcatalognumber_fkey FOREIGN KEY (productcatalognumber_id)
      REFERENCES productcatalognumbers_productcatalognumbers (id) DEFERRABLE;
	  
ALTER TABLE deliveries_deliveredproduct ADD COLUMN succession integer;

-- end


-- Table: productioncounting_productionrecord
-- changed: 19.12.2012

ALTER TABLE productioncounting_productionrecord ADD COLUMN subcontractor_id bigint;

ALTER TABLE productioncounting_productionrecord
  ADD CONSTRAINT productionrecord_subcontractor_fkey FOREIGN KEY (subcontractor_id)
      REFERENCES basic_company (id) DEFERRABLE;
	  
-- end


-- Table: deliveries_delivery
-- changed: 19.12.2012

ALTER TABLE deliveries_delivery ADD COLUMN location_id bigint;

ALTER TABLE deliveries_delivery
  ADD CONSTRAINT delivery_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE;
	  
-- end


-- Table: basic_product
-- changed: 19.12.2012

ALTER TABLE basic_product ADD COLUMN nodenumber character varying(255);
ALTER TABLE basic_product ADD COLUMN lastoffercost numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN lastoffercost SET DEFAULT 0::numeric;
ALTER TABLE basic_product ADD COLUMN averageoffercost numeric(12,5);
ALTER TABLE basic_product ALTER COLUMN averageoffercost SET DEFAULT 0::numeric;

-- end


-- Table: deliveries_delivery
-- changed: 19.12.2012

ALTER TABLE deliveries_delivery ADD COLUMN deliveryaddress character varying(2048);
ALTER TABLE deliveries_delivery ADD COLUMN externalnumber character varying(255);

-- end


-- Table: deliveries_delivery
-- changed: 19.12.2012

ALTER TABLE deliveries_delivery ADD COLUMN externalnumber character varying(255);
ALTER TABLE deliveries_delivery ADD COLUMN externalsynchronized boolean;
ALTER TABLE deliveries_delivery ALTER COLUMN externalsynchronized SET DEFAULT true;
ALTER TABLE deliveries_delivery ADD COLUMN targetstate character varying(255);

-- end


-- Table: basic_company
-- changed: 19.12.2012

ALTER TABLE basic_company ADD COLUMN externalnumber character varying(255);

-- end


-- Table: materialflow_location
-- changed: 19.12.2012

ALTER TABLE materialflow_location ADD COLUMN externalnumber character varying(255);

-- end

