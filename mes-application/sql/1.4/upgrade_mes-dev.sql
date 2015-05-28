-- Added field to PPS
-- last touched 01.06.2015 by kama

ALTER TABLE productionpershift_productionpershift ADD COLUMN orderfinishdate timestamp without time zone;

-- end

-- Changes in substitutes
-- last touched 12.05.2015 by kama

ALTER TABLE basic_substitutecomponent ADD COLUMN baseproduct_id bigint;
ALTER TABLE basic_substitutecomponent
  ADD CONSTRAINT substitutecomponent_baseproduct_fkey FOREIGN KEY (baseproduct_id)
      REFERENCES basic_product (id) DEFERRABLE;

-- end

-- Reworked menu, added category Company structure, added missing views to basic
-- last touched 13.05.2015 by kama

INSERT INTO qcadooview_category(
            id, pluginidentifier, name, succession)
    VALUES (nextval('hibernate_sequence'), 'basic', 'companyStructure', (
  SELECT succession FROM qcadooview_category WHERE name='basic'
    )+1);
 
INSERT INTO qcadooview_view(id,pluginidentifier, name, view) 
  VALUES (nextval('hibernate_sequence'),'basic','factoriesList','factoriesList');
INSERT INTO qcadooview_view(id,pluginidentifier, name, view) 
  VALUES (nextval('hibernate_sequence'),'basic','subassembliesList','subassembliesList');

INSERT INTO qcadooview_item(
            id, pluginidentifier, name, active, category_id, view_id, succession)
    VALUES (nextval('hibernate_sequence'), 'basic', 'subassemblies', TRUE, 
  (SELECT id FROM qcadooview_category WHERE name='companyStructure'), 
  (SELECT id FROM qcadooview_view WHERE name='subassembliesList'),6);

INSERT INTO qcadooview_item(
            id, pluginidentifier, name, active, category_id, view_id, succession)
    VALUES (nextval('hibernate_sequence'), 'basic', 'factories', TRUE, 
  (SELECT id FROM qcadooview_category WHERE name='companyStructure'), 
  (SELECT id FROM qcadooview_view WHERE name='factoriesList'), 1);


UPDATE qcadooview_item
   SET category_id= (
  SELECT id FROM qcadooview_category WHERE name='companyStructure'
   ),succession=2
 WHERE name='divisions';

UPDATE qcadooview_item
   SET category_id= (
  SELECT id FROM qcadooview_category WHERE name='companyStructure'
   ),succession=3
 WHERE name='productionLines';

 UPDATE qcadooview_item
   SET category_id= (
  SELECT id FROM qcadooview_category WHERE name='companyStructure'
   ),succession=4
 WHERE name='workstationTypes';
 
 UPDATE qcadooview_item
   SET category_id= (
  SELECT id FROM qcadooview_category WHERE name='companyStructure'
   ),succession=5
 WHERE name='workstations';

 -- end

 -- Added 'is default' field to delivered products in company
 -- last touched 14.05.2015 by kama


ALTER TABLE deliveries_companyproduct ADD COLUMN isdefault boolean;
ALTER TABLE deliveries_companyproduct ALTER COLUMN isdefault SET DEFAULT false;
ALTER TABLE deliveries_companyproductsfamily ADD COLUMN isdefault boolean;
ALTER TABLE deliveries_companyproductsfamily ALTER COLUMN isdefault SET DEFAULT false;

-- end

-- Table: warehouseminimalstate_warehouseminimumstate

-- DROP TABLE warehouseminimalstate_warehouseminimumstate;

CREATE TABLE warehouseminimalstate_warehouseminimumstate
(
  id bigint NOT NULL,
  product_id bigint,
  location_id bigint,
  minimumstate numeric(12,5),
  optimalorderquantity numeric(12,5),
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT warehouseminimalstate_warehouseminimumstate_pkey PRIMARY KEY (id),
  CONSTRAINT warehouseminimumstate_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT warehouseminimumstate_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

CREATE TABLE warehouseminimalstate_warehouseminimumstatemulti
(
  id bigint NOT NULL,
  location_id bigint,
  minimumstate numeric(12,5),
  optimalorderquantity numeric(12,5),
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT warehouseminimalstate_warehouseminimumstatemulti_pkey PRIMARY KEY (id),
  CONSTRAINT warehouseminimumstatemulti_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

CREATE TABLE jointable_product_warehouseminimumstatemulti
(
  product_id bigint NOT NULL,
  warehouseminimumstatemulti_id bigint NOT NULL,
  CONSTRAINT jointable_product_warehouseminimumstatemulti_pkey PRIMARY KEY (warehouseminimumstatemulti_id, product_id),
  CONSTRAINT warehouseminimalstate_warehouseminimumstatemulti_product_fkey FOREIGN KEY (warehouseminimumstatemulti_id)
      REFERENCES warehouseminimalstate_warehouseminimumstatemulti (id) DEFERRABLE,
  CONSTRAINT basic_product_warehouseminimumstatemulti_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end

-- Added updating cost norms for products
-- last touched 28.05.2015 by kama

CREATE TABLE materialflowresources_costnormsgenerator
(
  id bigint NOT NULL,
  costssource character varying(255) DEFAULT '01mes'::character varying,
  productstoupdate character varying(255) DEFAULT '01all'::character varying,
  CONSTRAINT materialflowresources_costnormsgenerator_pkey PRIMARY KEY (id)
);

CREATE TABLE materialflowresources_costnormslocation
(
  id bigint NOT NULL,
  costnormsgenerator_id bigint,
  location_id bigint,
  CONSTRAINT materialflowresources_costnormslocation_pkey PRIMARY KEY (id),
  CONSTRAINT costnormslocation_costnormsgenerator_fkey FOREIGN KEY (costnormsgenerator_id)
      REFERENCES materialflowresources_costnormsgenerator (id) DEFERRABLE,
  CONSTRAINT costnormslocation_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

ALTER TABLE basic_product ADD COLUMN costnormsgenerator_id bigint;
ALTER TABLE basic_product
  ADD CONSTRAINT product_costnormsgenerator_fkey FOREIGN KEY (costnormsgenerator_id)
      REFERENCES materialflowresources_costnormsgenerator (id) DEFERRABLE;

-- end