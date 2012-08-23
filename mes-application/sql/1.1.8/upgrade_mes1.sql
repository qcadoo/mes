-- Table: productcatalognumbers_productcatalognumbers

-- change: 17.08.2012

CREATE TABLE productcatalognumbers_productcatalognumbers
(
  id bigint NOT NULL,
  catalognumber character varying(256),
  product_id bigint,
  company_id bigint,
  CONSTRAINT productcatalognumbers_productcatalognumbers_pkey PRIMARY KEY (id),
  CONSTRAINT basic_company_fkey 
    FOREIGN KEY (company_id)
      REFERENCES basic_company (id) 
     DEFERRABLE,
  CONSTRAINT basic_product_fkey 
    FOREIGN KEY (product_id)
       REFERENCES basic_product (id) 
  DEFERRABLE
);
--end

-- Table: basic_product

-- change: 23.08.2012
ALTER TABLE basic_product ADD COLUMN entitytype character varying(255);
ALTER TABLE basic_product ALTER COLUMN entitytype SET DEFAULT '01particularProduct'::character varying;

UPDATE  basic_product SET entitytype = '01particularProduct';

ALTER TABLE basic_product ADD COLUMN parent_id bigint;
ADD CONSTRAINT basic_product_fkey 
	FOREIGN KEY (parent_id)
      REFERENCES basic_product (id)
  DEFERRABLE;

--end


 