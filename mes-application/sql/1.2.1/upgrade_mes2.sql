-- Table: basic_country
-- changed: 24.04.2013

CREATE TABLE basic_country
(
  id bigint NOT NULL,
  code character varying(2),
  country character varying(255),
  CONSTRAINT basic_country_pkey PRIMARY KEY (id)
);

-- end


-- Table: basic_parameter
-- changed: 24.04.2013

ALTER TABLE basic_parameter ADD COLUMN country_id bigint;

ALTER TABLE basic_parameter
  ADD CONSTRAINT parameter_country_fkey FOREIGN KEY (country_id)
      REFERENCES basic_country (id) DEFERRABLE;
      
-- end


-- Table: basic_company
-- changed: 24.04.2013

ALTER TABLE basic_company ADD COLUMN country_id bigint;

ALTER TABLE basic_company
  ADD CONSTRAINT company_country_fkey FOREIGN KEY (country_id)
      REFERENCES basic_country (id) DEFERRABLE;
      
ALTER TABLE basic_company ADD COLUMN taxcountrycode_id bigint;      

ALTER TABLE basic_company
  ADD CONSTRAINT company_taxtcountrycode_fkey FOREIGN KEY (taxcountrycode_id)
      REFERENCES basic_country (id) DEFERRABLE;
      
-- end


-- Table: deliveries_parameterdeliveryordercolumn
-- changed: 09.05.2013

CREATE TABLE deliveries_parameterdeliveryordercolumn
(
  id bigint NOT NULL,
  parameter_id bigint,
  columnfororders_id bigint,
  succession integer,
  CONSTRAINT deliveries_parameterdeliveryordercolumn_pkey PRIMARY KEY (id),
  CONSTRAINT parameterdeliveryordercolumn_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) DEFERRABLE,
  CONSTRAINT parameterdeliveryordercolumn_columnfororders_fkey FOREIGN KEY (columnfororders_id)
      REFERENCES deliveries_columnfororders (id) DEFERRABLE
);

-- end


-- Table: deliveries_delivery
-- changed: 16.07.2013

ALTER TABLE deliveries_delivery ADD COLUMN paymentform character varying(1024);

-- end


-- Table: basicproductioncounting_basicproductioncounting
-- changed: 19.07.2013

ALTER TABLE basicproductioncounting_basicproductioncounting DROP COLUMN plannedquantity;

-- end


-- Table: orders_order
-- changed: 19.07.2013

ALTER TABLE orders_order DROP COLUMN alert;
ALTER TABLE orders_order DROP COLUMN ignoremissingrequiredcomponents;
ALTER TABLE orders_order DROP COLUMN requiredcomponent_id;

-- end


-- Table: deliveries_delivery
-- changed: 19.07.2013

ALTER TABLE deliveries_delivery DROP COLUMN delivery_id;

-- end


-- Table: productioncounting_recordoperationproductincomponent
-- changed: 19.07.2013

ALTER TABLE productioncounting_recordoperationproductincomponent DROP COLUMN plannedquantity;

-- end


-- Table: productioncounting_recordoperationproductoutcomponent
-- changed: 19.07.2013

ALTER TABLE productioncounting_recordoperationproductoutcomponent DROP COLUMN plannedquantity;

-- end


-- Table: productionpershift_productionpershift
-- changed: 19.07.2013

ALTER TABLE  productionpershift_productionpershift DROP COLUMN technologyinstanceoperationcomponent_id;

-- end


-- Table: technologies_operation
-- changed: 19.07.2013

ALTER TABLE technologies_operation DROP COLUMN machine_id;

-- end


-- Table: masterorders_masterorder
-- changed: 26.07.2013

ALTER TABLE masterorders_masterorder ADD COLUMN active boolean;
ALTER TABLE masterorders_masterorder ALTER COLUMN active SET DEFAULT true;

-- end


-- Table: deliveries_delivery
-- changed: 29.07.2013

ALTER TABLE deliveries_delivery ADD COLUMN currency_id bigint;

-- end


-- Table: technologies_technology
-- changed: 30.07.2013

ALTER TABLE technologies_technology ADD COLUMN externalsynchronized boolean;
ALTER TABLE technologies_technology ALTER COLUMN externalsynchronized SET DEFAULT true;

UPDATE technologies_technology SET externalsynchronized = true;

-- end

