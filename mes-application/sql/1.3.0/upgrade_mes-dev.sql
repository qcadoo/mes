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
update qcadooview_category set pluginidentifier='advancedGenealogy' where pluginidentifier='genealogies' AND name='advancedGenealogy';
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

-- Table: technologies_barcodeoperationcomponent
-- create: 14.07.2014

CREATE TABLE technologies_barcodeoperationcomponent
(
  id BIGINT NOT NULL,
  operationcomponent_id BIGINT,
  code character varying(20),
  active boolean DEFAULT true,
  CONSTRAINT technologies_barcodeoperationcomponent_pkey PRIMARY KEY (id),
  CONSTRAINT barcodeoperationcomponen_operationcomponent_fkey FOREIGN KEY (operationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE
);
-- end

--  Add 'update date' field to the orders' deviation/correction reason type models.
-- last touched at 09.07.2014 by maku

ALTER TABLE orders_reasontypecorrectiondatefrom ADD COLUMN "date" TIMESTAMP;
ALTER TABLE orders_reasontypecorrectiondateto ADD COLUMN "date" TIMESTAMP;
ALTER TABLE orders_reasontypedeviationeffectivestart ADD COLUMN "date" TIMESTAMP;
ALTER TABLE orders_reasontypedeviationeffectiveend ADD COLUMN "date" TIMESTAMP;
ALTER TABLE orders_typeofcorrectioncauses ADD COLUMN "date" TIMESTAMP;

-- end

-- Add view and menu item for order deviations report
-- last touched at 30.07.2014 by maku

INSERT INTO qcadooview_view (id, pluginidentifier, name, view)
VALUES (nextval('hibernate_sequence'), 'deviationCausesReporting', 'deviationsReportGenerator', 'deviationsReportGenerator');
INSERT INTO qcadooview_item (id, pluginidentifier, name, active,
        category_id,
        view_id,
        succession)
VALUES (nextval('hibernate_sequence'), 'deviationCausesReporting', 'deviationsReport', TRUE,
        (SELECT
           id
         FROM qcadooview_category
         WHERE name = 'orders'),
        (SELECT
           id
         FROM qcadooview_view
         WHERE name =
               'deviationsReportGenerator'),
        (SELECT
           max(succession) + 1
         FROM qcadooview_item
         WHERE category_id = (SELECT
                                id
                              FROM qcadooview_category
                              WHERE name = 'orders')
        )
);

-- end


-- Add exchangeRate to Currency in mes-plugins-basic
-- last touched 17.07.2014 by adso

ALTER TABLE basic_currency ADD COLUMN "exchangerate" NUMERIC(12, 5) DEFAULT 1;

-- end

-- Table: basic_parameter
-- create: 22.07.2014

ALTER TABLE basic_parameter ADD COLUMN hidebarcodeoperationcomponentinworkplans boolean;

-- end

-- Enable new plugin deviationCausesReporting and its dependency (PPS)
-- last touched 30.07.2014 by maku

UPDATE qcadooplugin_plugin SET state = 'ENABLING'
  WHERE identifier = 'productionPerShift' AND NOT EXISTS (SELECT id FROM qcadooplugin_plugin WHERE identifier = 'productionPerShift' AND state = 'ENABLED');

INSERT INTO qcadooplugin_plugin (id, identifier, version, state, issystem)
VALUES (nextval('hibernate_sequence'), 'deviationCausesReporting', '1.3.0', 'ENABLING', false);

-- end
