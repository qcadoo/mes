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
-- change: 06.09.2012

ALTER TABLE technologies_operation ADD COLUMN issubcontracting boolean;
ALTER TABLE technologies_operation ALTER COLUMN issubcontracting SET DEFAULT false;

-- end


-- Table: technologies_technologyoperationcomponent
-- change: 06.09.2012

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN issubcontracting boolean;
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN issubcontracting SET DEFAULT false;

-- end


-- Table: technologies_technologyinstanceoperationcomponent
-- change: 06.09.2012

ALTER TABLE technologies_technologyinstanceoperationcomponent ADD COLUMN issubcontracting boolean;
ALTER TABLE technologies_technologyinstanceoperationcomponent ALTER COLUMN issubcontracting SET DEFAULT false;

-- end


-- Table materialrequirements_materialrequirement
-- change: 12.09.2012

ALTER TABLE materialrequirements_materialrequirement ADD COLUMN "number" character varying(256);
ALTER TABLE materialrequirements_materialrequirement ADD COLUMN mrpalgorithm character varying(255);
ALTER TABLE materialrequirements_materialrequirement ALTER COLUMN mrpalgorithm SET DEFAULT '01onlyComponents'::character varying;

UPDATE materialrequirements_materialrequirement SET mrpalgorithm = '01onlyComponents' WHERE onlyComponents = true;
UPDATE materialrequirements_materialrequirement SET mrpalgorithm = '02allProductsIn' WHERE onlyComponents = false;

ALTER TABLE  materialrequirements_materialrequirement  DROP COLUMN  onlycomponents;

-- end


-- Table materialrequirements_materialrequirement
-- change: 12.09.2012

ALTER TABLE simplematerialbalance_simplematerialbalance ADD COLUMN mrpalgorithm character varying(255);
ALTER TABLE simplematerialbalance_simplematerialbalance ALTER COLUMN mrpalgorithm SET DEFAULT '01onlyComponents'::character varying;

UPDATE simplematerialbalance_simplematerialbalance SET mrpalgorithm = '01onlyComponents' WHERE onlyComponents = true;
UPDATE simplematerialbalance_simplematerialbalance SET mrpalgorithm = '02allProductsIn' WHERE onlyComponents = false;

ALTER TABLE  simplematerialbalance_simplematerialbalance  DROP COLUMN onlycomponents;

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
      REFERENCES productionlines_productionline (id) DEFERRABLE
);

-- end


-- Table: assignmenttoshift_assignmenttoshift
-- change: 18.09.2012

ALTER TABLE assignmenttoshift_assignmenttoshift ADD COLUMN active boolean;
ALTER TABLE assignmenttoshift_assignmenttoshift ALTER COLUMN active SET DEFAULT true;

-- end


-- Table: assignmenttoshift_assignmenttoshiftreport
-- change: 18.09.2012

ALTER TABLE assignmenttoshift_assignmenttoshiftreport ADD COLUMN active boolean;
ALTER TABLE assignmenttoshift_assignmenttoshiftreport ALTER COLUMN active SET DEFAULT true;

-- end


-- Table technologies_operationgroup
-- change: 18.09.2012

CREATE TABLE technologies_operationgroup
(
  id bigint NOT NULL,
  "number" character varying(255),
  "name" character varying(1024),
  CONSTRAINT technologies_operationgroup_pkey PRIMARY KEY (id)
);

-- end


-- Table technologies_operationgroup
-- change: 18.09.2012

ALTER TABLE technologies_operation ADD COLUMN operationgroup_id bigint;

ALTER TABLE technologies_operation
  ADD CONSTRAINT operation_operationgroup_fkey FOREIGN KEY (operationgroup_id)
      REFERENCES technologies_operationgroup (id) DEFERRABLE;
      
-- end


-- Table operationaltasks_operationaltask
-- change: 20.09.2012

ALTER TABLE operationaltasks_operationaltask ADD COLUMN technologyinstanceoperationcomponent_id bigint;
ALTER TABLE operationaltasks_operationaltask
	ADD CONSTRAINT technologies_technologyinstanceoperationcomponent_fkey FOREIGN KEY (technologyinstanceoperationcomponent_id)
      REFERENCES technologies_technologyinstanceoperationcomponent (id) DEFERRABLE;
      
ALTER TABLE operationaltasks_operationaltask ADD COLUMN order_id bigint;    
ALTER TABLE operationaltasks_operationaltask
	ADD CONSTRAINT orders_order_fkey FOREIGN KEY (order_id)
      REFERENCES orders_order (id) DEFERRABLE; 
      
-- end   

      
-- Table avglaborcostcalcfororder_avglaborcostcalcfororder
-- change: 27.09.2012

ALTER TABLE avglaborcostcalcfororder_avglaborcostcalcfororder ALTER COLUMN startdate TYPE date;
ALTER TABLE avglaborcostcalcfororder_avglaborcostcalcfororder ALTER COLUMN finishdate TYPE date;

-- end
      
      
-- Table: deliveries_delivery
-- change: 27.09.2012

CREATE TABLE deliveries_delivery
(
  id bigint NOT NULL,
  "number" character varying(255),
  "name" character varying(1024),
  description character varying(2048),
  state character varying(255),
  supplier_id bigint,
  deliverydate timestamp without time zone,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  "createuser" character varying(255),
  updateuser character varying(255),
  delivery_id bigint,
  CONSTRAINT deliveries_delivery_pkey PRIMARY KEY (id),
  CONSTRAINT deliveries_delivery_fkey FOREIGN KEY (delivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE,
  CONSTRAINT basic_company_fkey FOREIGN KEY (supplier_id)
      REFERENCES basic_company (id) DEFERRABLE
);

-- end


-- Table: deliveries_deliverystatechange
-- change: 27.09.2012

CREATE TABLE deliveries_deliverystatechange
(
  id bigint NOT NULL,
  dateandtime timestamp without time zone,
  sourcestate character varying(255),
  targetstate character varying(255),
  status character varying(255),
  phase integer,
  worker character varying(255),
  delivery_id bigint,
  shift_id bigint,
  CONSTRAINT deliveries_deliverystatechange_pkey PRIMARY KEY (id),
  CONSTRAINT deliveries_delivery_fkey FOREIGN KEY (delivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE,
  CONSTRAINT basic_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE
);

-- end


-- Table: deliveries_orderedproduct
-- change: 27.09.2012

CREATE TABLE deliveries_orderedproduct
(
  id bigint NOT NULL,
  delivery_id bigint,
  product_id bigint,
  orderedquantity numeric(12,5),
  CONSTRAINT deliveries_orderedproduct_pkey PRIMARY KEY (id),
  CONSTRAINT deliveries_delivery_fkey FOREIGN KEY (delivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE,
  CONSTRAINT basic_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end


-- Table: deliveries_deliveredproduct
-- change: 27.09.2012

CREATE TABLE deliveries_deliveredproduct
(
  id bigint NOT NULL,
  delivery_id bigint,
  product_id bigint,
  deliveredquantity numeric(12,5),
  damagedquantity numeric(12,5),
  CONSTRAINT deliveries_deliveredproduct_pkey PRIMARY KEY (id),
  CONSTRAINT deliveries_delivery_fkey FOREIGN KEY (delivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE,
  CONSTRAINT basic_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end

-- Table: states_message
-- change: 27.09.2012
ALTER TABLE states_message ADD COLUMN deliverystatechange_id bigint;

ALTER TABLE states_message
  ADD CONSTRAINT deliveries_deliverystatechange_fkey FOREIGN KEY (deliverystatechange_id)
      REFERENCES deliveries_deliverystatechange (id) DEFERRABLE;

-- end

-- Table: basic_product
-- change: 02.10.2012

ALTER TABLE basic_product ADD COLUMN companyproduct_id bigint;
ALTER TABLE basic_product
ADD CONSTRAINT companyproduct_fkey FOREIGN KEY (companyproduct_id)
      REFERENCES basic_company (id) DEFERRABLE;
      
ALTER TABLE basic_product ADD COLUMN companyproductfamily_id bigint;

ALTER TABLE basic_product
ADD CONSTRAINT companyproductfamily_fkey FOREIGN KEY (companyproductfamily_id)
      REFERENCES basic_company (id) DEFERRABLE;
      
-- end

-- Table: basic_company
-- change: 02.10.2012

ALTER TABLE basic_company ADD COLUMN buffer integer;

-- end


-- Table: qcadoocustomtranslation_customtranslation
-- change: 02.10.2012

CREATE TABLE qcadoocustomtranslation_customtranslation
(
  id bigint NOT NULL,
  pluginidentifier character varying(255),
  key character varying(255),
  customtranslation character varying(255),
  active boolean DEFAULT false,
  locale character varying(255),
  CONSTRAINT qcadoocustomtranslation_customtranslation_pkey PRIMARY KEY (id )
);

-- end
