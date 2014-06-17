-- Table: technologies_productstructuretreenode

-- changed: 29.05.2014

CREATE TABLE technologies_productstructuretreenode
(
  id bigint NOT NULL,
  priority integer,
  technology_id bigint,
  parent_id BIGINT,
  nodenumber VARCHAR(255),
  "number" character varying(255),
  product_id bigint,
  createDate timestamp,
  updateDate timestamp,
  createUser character varying(255),
  updateUser character varying(255),
  CONSTRAINT technologies_productstructuretreenode_pkey PRIMARY KEY (id),
  CONSTRAINT productstructuretreenode_parent_fkey FOREIGN KEY (parent_id)
      REFERENCES technologies_productstructuretreenode (id) DEFERRABLE,
  CONSTRAINT productstructuretreenode_technology_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE
);

-- end

-- Delete genealogies, genealogiesForComponent and qualityControlsForBatch view items.
-- last touched 17.06.2014 by maku

delete from qcadooview_item where pluginidentifier in ('genealogies', 'genealogiesForComponents', 'qualityControlsForBatch');
delete from qcadooview_view where pluginidentifier in ('genealogies', 'genealogiesForComponents', 'qualityControlsForBatch');
delete from qcadooview_category where pluginidentifier in ('genealogies', 'genealogiesForComponents', 'qualityControlsForBatch');
delete from qcadoomodel_dictionary where pluginidentifier in ('genealogies', 'genealogiesForComponents', 'qualityControlsForBatch');

--end


-- Add material flow resources' DDLs
-- last touched 17.06.2014 by tola

CREATE TABLE materialflowresources_document
(
  id              BIGINT NOT NULL,
  number          VARCHAR(255),
  type            VARCHAR(255),
  time            TIMESTAMP,
  state           VARCHAR(255) DEFAULT '01draft',
  locationfrom_id BIGINT,
  locationto_id   BIGINT,
  user_id         BIGINT,
  delivery_id     BIGINT,
  active          BOOL DEFAULT TRUE,
  createdate      TIMESTAMP,
  updatedate      TIMESTAMP,
  createuser      VARCHAR(255),
  updateuser      VARCHAR(255),
  CONSTRAINT document_id_pkey PRIMARY KEY (id),
  CONSTRAINT document_delivery_fkey FOREIGN KEY (delivery_id) REFERENCES deliveries_delivery (id) DEFERRABLE,
  CONSTRAINT document_locationto_fkey FOREIGN KEY (locationto_id) REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT document_locationfrom_fkey FOREIGN KEY (locationfrom_id) REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT document_user_fkey FOREIGN KEY (user_id) REFERENCES qcadoosecurity_user (id) DEFERRABLE
);

CREATE TABLE materialflowresources_position
(
  id             BIGINT NOT NULL,
  document_id    BIGINT,
  product_id     BIGINT,
  quantity       NUMERIC(12),
  price          NUMERIC(12) DEFAULT 0 :: NUMERIC,
  batch          VARCHAR(255),
  productiondate DATE,
  expirationdate DATE,
  number         INT,
  CONSTRAINT position_id_pkey PRIMARY KEY (id),
  CONSTRAINT position_product_fkey FOREIGN KEY (product_id) REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT position_document_fkey FOREIGN KEY (document_id) REFERENCES materialflowresources_document (id) DEFERRABLE
);

ALTER TABLE materialflowresources_resource ADD COLUMN productiondate DATE;
ALTER TABLE materialflowresources_resource ADD COLUMN expirationdate DATE;

CREATE OR REPLACE VIEW materialflowresources_warehousestock AS
    SELECT row_number() OVER () AS id, location_id, product_id, SUM(quantity) AS quantity
    FROM materialflowresources_resource GROUP BY location_id, product_id ;

ALTER TABLE materialflow_location ADD COLUMN algorithm VARCHAR(255) DEFAULT '01fifo';
ALTER TABLE materialflow_location ADD COLUMN requireprice BOOL;
ALTER TABLE materialflow_location ADD COLUMN requirebatch BOOL;
ALTER TABLE materialflow_location ADD COLUMN requireproductiondate BOOL;
ALTER TABLE materialflow_location ADD COLUMN requireexpirationdate BOOL;


-- end