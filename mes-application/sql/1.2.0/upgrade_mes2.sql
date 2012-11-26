-- Table: basic_product
-- changed: 23.11.2012

ALTER TABLE basic_product DROP COLUMN companyproduct_id;
ALTER TABLE basic_product ADD COLUMN companyproductfamily_id bigint;
      
-- end


-- Table: technologies_operation
-- changed: 23.11.2012

ALTER TABLE technologies_operation DROP COLUMN company_id bigint;

-- end

      
-- Table: technologies_operationgroup
-- changed: 23.11.2012

ALTER TABLE technologies_operationgroup DROP COLUMN companyoperationgroup_id bigint;

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


-- Table: basic_product
-- changed: 23.11.2012

UPDATE basic_product SET inputproductsrequiredfortype = '01startOrder' WHERE inputproductsrequiredfortype IS null;

-- end


-- Table: basic_product
-- changed: 23.11.2012

UPDATE orders_order SET inputproductsrequiredfortype = '01startOrder' WHERE inputproductsrequiredfortype IS null;

-- end
