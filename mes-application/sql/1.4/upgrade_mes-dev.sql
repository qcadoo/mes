-- Added unit conversions to OPIC, positions and products in RR
-- last touched 14.04.2015 by kama

ALTER TABLE technologies_operationproductincomponent ADD COLUMN givenquantity numeric(14,5);
ALTER TABLE technologies_operationproductincomponent ADD COLUMN givenunit character varying(255);

ALTER TABLE materialflowresources_position ADD COLUMN givenunit character varying(255);
ALTER TABLE materialflowresources_position ADD COLUMN givenquantity numeric(14,5);

ALTER TABLE productioncounting_trackingoperationproductincomponent ADD COLUMN givenunit character varying(255);
ALTER TABLE productioncounting_trackingoperationproductincomponent ADD COLUMN givenquantity numeric(14,5);

ALTER TABLE productioncounting_trackingoperationproductoutcomponent ADD COLUMN givenunit character varying(255);
ALTER TABLE productioncounting_trackingoperationproductoutcomponent ADD COLUMN givenquantity numeric(14,5);

-- end

-- Added factories (DUR)
-- last touched 27.04.2015 by kama 

CREATE TABLE basic_factory
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(1024),
  city character varying(255),
  active boolean DEFAULT true,
  CONSTRAINT basic_factory_pkey PRIMARY KEY (id)
);

ALTER TABLE basic_division ADD COLUMN factory_id bigint;
ALTER TABLE basic_division
  ADD CONSTRAINT division_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE;

-- end

-- Scripts for DUR
-- last touched 04.05.2015 by kama

CREATE TABLE jointable_company_workstation
(
  company_id bigint NOT NULL,
  workstation_id bigint NOT NULL,
  CONSTRAINT jointable_company_workstation_pkey PRIMARY KEY (workstation_id, company_id),
  CONSTRAINT jointable_company_workstation_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT jointable_company_workstation_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE
);

ALTER TABLE basic_workstation ADD COLUMN division_id bigint;
ALTER TABLE basic_workstation
  ADD CONSTRAINT workstation_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE;
ALTER TABLE basic_workstation ADD COLUMN serialnumber character varying(255);
ALTER TABLE basic_workstation ADD COLUMN udtnumber character varying(255);
ALTER TABLE basic_workstation ADD COLUMN series character varying(255);
ALTER TABLE basic_workstation ADD COLUMN producer character varying(255);
ALTER TABLE basic_workstation ADD COLUMN productiondate date;
ALTER TABLE basic_workstation ADD COLUMN wnknumber character varying(255);

CREATE TABLE basic_workstationattachment
(
  id bigint NOT NULL,
  workstation_id bigint,
  attachment character varying(255),
  name character varying(255),
  size numeric(12,5),
  ext character varying(255),
  CONSTRAINT basic_workstationattachment_pkey PRIMARY KEY (id),
  CONSTRAINT workstationattachment_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE
);

CREATE TABLE jointable_division_productionline
(
  productionline_id bigint NOT NULL,
  division_id bigint NOT NULL,
  CONSTRAINT jointable_division_productionline_pkey PRIMARY KEY (division_id, productionline_id),
  CONSTRAINT jointable_division_productionline_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT jointable_division_productionline_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE
);

CREATE TABLE basic_subassembly
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(1024),
  workstationtype_id bigint,
  workstation_id bigint,
  serialnumber character varying(255),
  series character varying(255),
  producer character varying(255),
  productiondate date,
  lastrepairsdate date,
  active boolean DEFAULT true,
  CONSTRAINT basic_subassembly_pkey PRIMARY KEY (id),
  CONSTRAINT subassembly_workstationtype_fkey FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) DEFERRABLE,
  CONSTRAINT subassembly_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE
);

CREATE TABLE jointable_company_subassembly
(
  company_id bigint NOT NULL,
  subassembly_id bigint NOT NULL,
  CONSTRAINT jointable_company_subassembly_pkey PRIMARY KEY (subassembly_id, company_id),
  CONSTRAINT jointable_company_subassembly_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT jointable_company_subassembly_subassembly_fkey FOREIGN KEY (subassembly_id)
      REFERENCES basic_subassembly (id) DEFERRABLE
);

CREATE TABLE basic_subassemblyattachment
(
  id bigint NOT NULL,
  subassembly_id bigint,
  attachment character varying(255),
  name character varying(255),
  size numeric(12,5),
  ext character varying(255),
  CONSTRAINT basic_subassemblyattachment_pkey PRIMARY KEY (id),
  CONSTRAINT subassemblyattachment_subassembly_fkey FOREIGN KEY (subassembly_id)
      REFERENCES basic_subassembly (id) DEFERRABLE
);

ALTER TABLE basic_workstationtype ADD COLUMN subassembly boolean;

ALTER TABLE basic_division ADD COLUMN active boolean;
ALTER TABLE basic_division ALTER COLUMN active SET DEFAULT true;
ALTER TABLE basic_division ADD COLUMN comment character varying(2048);

CREATE TABLE productionlines_factorystructureelement
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(255),
  parent_id bigint,
  priority integer,
  nodenumber character varying(255),
  entitytype character varying(255) DEFAULT 'factory'::character varying,
  current boolean DEFAULT false,
  subassembly_id bigint,
  workstation_id bigint,
  CONSTRAINT productionlines_factorystructureelement_pkey PRIMARY KEY (id),
  CONSTRAINT factorystructureelement_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE,
  CONSTRAINT factorystructureelement_parent_fkey FOREIGN KEY (parent_id)
      REFERENCES productionlines_factorystructureelement (id) DEFERRABLE,
  CONSTRAINT factorystructureelement_subassembly_fkey FOREIGN KEY (subassembly_id)
      REFERENCES basic_subassembly (id) DEFERRABLE
);

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