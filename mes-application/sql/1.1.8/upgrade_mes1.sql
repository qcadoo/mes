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
    	DEFERRABLE;
  CONSTRAINT basic_product_fkey 
  		FOREIGN KEY (product_id)
      	REFERENCES basic_product (id) 
		DEFERRABLE;
);
--end