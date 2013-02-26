-- Table: materialflow_transfer
-- changed: 25.02.2013

ALTER TABLE materialflow_transfer ADD COLUMN fromdelivery_id bigint;

ALTER TABLE materialflow_transfer
  ADD CONSTRAINT transfer_delivery_fkey FOREIGN KEY (fromdelivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE;

-- end


-- Table: masterorders_masterorder
-- changed: 26.02.2013

CREATE TABLE masterorders_masterorder
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(1024),
  description character varying(2048),
  externalnumber character varying(255),
  deadline timestamp without time zone,
  addmasterprefixtonumber boolean,
  masterorderquantity numeric(12,5),
  cumulatedorderquantity numeric(12,5),
  masterordertype character varying(255) DEFAULT '01undefined'::character varying,
  masterorderstate character varying(255),
  company_id bigint,
  product_id bigint,
  technology_id bigint,
  externalsynchronized boolean DEFAULT true,
  CONSTRAINT masterorders_masterorder_pkey PRIMARY KEY (id),
  CONSTRAINT masterorder_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT masterorder_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT masterorder_technology_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE
);

-- end


-- Table: masterorders_masterorderproduct
-- changed: 26.02.2013

CREATE TABLE masterorders_masterorderproduct
(
  id bigint NOT NULL,
  product_id bigint,
  technology_id bigint,
  masterorder_id bigint,
  masterorderquantity numeric(12,5),
  cumulatedorderquantity numeric(12,5),
  CONSTRAINT masterorders_masterorderproduct_pkey PRIMARY KEY (id),
  CONSTRAINT fk215b549b4a728bc8 FOREIGN KEY (masterorder_id)
      REFERENCES masterorders_masterorder (id) DEFERRABLE,
  CONSTRAINT masterorderproduct_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT masterorderproduct_technology_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE
);

-- end


-- Table: orders_order
-- changed: 26.02.2013

ALTER TABLE orders_order ADD COLUMN masterorder_id bigint;
ALTER TABLE orders_order
  ADD CONSTRAINT order_masterorder_fkey FOREIGN KEY (masterorder_id)
      REFERENCES masterorders_masterorder (id) DEFERRABLE;

-- end


-- Table: orders_order
-- changed: 26.02.2013

ALTER TABLE orders_order DROP COLUMN batchnumber;

-- end
