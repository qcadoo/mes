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
-- change: 30.08.2012

CREATE TABLE basic_conversion
(
  id bigint NOT NULL,
  CONSTRAINT basic_conversion_pkey PRIMARY KEY (id )
);

-- end


-- Table: basic_conversionitem
-- change: 30.08.2012

CREATE TABLE basic_conversionitem
(
  id bigint NOT NULL,
  quantityfrom numeric(12,5),
  quantityto numeric(12,5),
  unitfrom character varying(255),
  unitto character varying(255),
  conversion_id bigint,
  product_id bigint,
  CONSTRAINT basic_conversionitem_pkey PRIMARY KEY (id ),
  CONSTRAINT conversionitem_conversion_fkey FOREIGN KEY (conversion_id)
      REFERENCES basic_conversion (id) DEFERRABLE,
  CONSTRAINT conversionitem_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end


-- Table: technologies_technologyoperationcomponent
-- change: 30.08.2012

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN comment character varying(2048);

-- end


-- Table: technologies_technologyinstanceoperationcomponent
-- change: 30.08.2012

ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN comment character varying(2048);
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN attachment character varying(255);

-- end

-- Table: technologies_operation
-- change: 6.09.2012
ALTER TABLE technologies_operation ADD COLUMN issubcontracting boolean;
ALTER TABLE technologies_operation ALTER COLUMN issubcontracting SET DEFAULT false;
-- end

-- Table: technologies_technologyoperationcomponent
-- change: 6.09.2012
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN issubcontracting boolean;
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN issubcontracting SET DEFAULT false;
-- end

-- Table: technologies_technologyinstanceoperationcomponent
-- change: 6.09.2012
ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN issubcontracting boolean;
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN issubcontracting SET DEFAULT false;
-- end


-- Table materialrequirements_materialrequirement
-- change: 12.09.2012
ALTER TABLE materialrequirements_materialrequirement ADD COLUMN "number" character varying(256);
ALTER TABLE materialrequirements_materialrequirement ADD COLUMN mrpalgorithm character varying(255);
ALTER TABLE materialrequirements_materialrequirement ALTER COLUMN mrpalgorithm SET DEFAULT '01onlyComponents'::character varying;

UPDATE materialrequirements_materialrequirement SET mrpalgorithm = '01onlyComponents' WHERE  onlyComponents=true;
UPDATE materialrequirements_materialrequirement SET mrpalgorithm = '02allProductsIn' WHERE  onlyComponents=false;

ALTER TABLE  materialrequirements_materialrequirement  DROP COLUMN  onlycomponents ;
-- end


-- Table materialrequirements_materialrequirement
-- change: 12.09.2012
ALTER TABLE simplematerialbalance_simplematerialbalance ADD COLUMN mrpalgorithm character varying(255);
ALTER TABLE simplematerialbalance_simplematerialbalance ALTER COLUMN mrpalgorithm SET DEFAULT '01onlyComponents'::character varying;

UPDATE simplematerialbalance_simplematerialbalance SET mrpalgorithm = '01onlyComponents' WHERE  onlyComponents=true;
UPDATE simplematerialbalance_simplematerialbalance SET mrpalgorithm = '02allProductsIn' WHERE  onlyComponents=false;

ALTER TABLE  simplematerialbalance_simplematerialbalance  DROP COLUMN  onlycomponents ;
-- end


-- Table operationaltasks_operationaltask
-- change: 13.09.2012
CREATE TABLE operationaltasks_operationaltask
(
  id bigint NOT NULL,
  "number" character varying(256),
  "name" character varying(1024),
  description character varying(1024),
  typetask character varying(255),
  startdate timestamp without time zone,
  finishdate timestamp without time zone,
  productionline_id bigint,
  CONSTRAINT operationaltasks_operationaltask_pkey PRIMARY KEY (id),
   CONSTRAINT productionlines_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
);
--end
