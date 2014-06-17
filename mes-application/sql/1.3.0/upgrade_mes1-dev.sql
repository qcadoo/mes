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