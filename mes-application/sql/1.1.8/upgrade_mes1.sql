-- Table: productcatalognumbers_productcatalognumbers
-- change: 17.08.2012

CREATE TABLE productcatalognumbers_productcatalognumbers
(
  id bigint NOT NULL,
  catalognumber character varying(256),
  product_id bigint,
  company_id bigint,
  CONSTRAINT productcatalognumbers_productcatalognumbers_pkey PRIMARY KEY (id),
  CONSTRAINT productcatalognumbers_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT productcatalognumbers_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end


-- Table: basic_product
-- change: 23.08.2012

ALTER TABLE basic_product ADD COLUMN entitytype character varying(255);
ALTER TABLE basic_product ALTER COLUMN entitytype SET DEFAULT '01particularProduct'::character varying;

UPDATE  basic_product SET entitytype = '01particularProduct';

ALTER TABLE basic_product ADD COLUMN parent_id bigint;
ALTER TABLE basic_product ADD CONSTRAINT basic_product_fkey 
	FOREIGN KEY (parent_id)
      REFERENCES basic_product (id)
    DEFERRABLE;

-- end


-- Table: basic_conversion
-- change: 24.08.2012

CREATE TABLE basic_conversion
(
  id bigint NOT NULL,
  active boolean DEFAULT true,
  CONSTRAINT basic_conversion_pkey PRIMARY KEY (id )
);

-- end


-- Table: basic_conversionitem
-- change: 24.08.2012

CREATE TABLE basic_conversionitem
(
  id bigint NOT NULL,
  quantityfrom character varying(255),
  quantityto character varying(255),
  unitfrom character varying(255),
  unitto character varying(255),
  conversion_id bigint,
  product_id bigint,
  active boolean DEFAULT true,
  CONSTRAINT basic_conversionitem_pkey PRIMARY KEY (id ),
  CONSTRAINT conversionitem_conversion_fkey FOREIGN KEY (conversion_id)
      REFERENCES basic_conversion (id) DEFERRABLE,
  CONSTRAINT conversionitem_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end