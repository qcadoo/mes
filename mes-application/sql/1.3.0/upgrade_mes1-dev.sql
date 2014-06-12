-- Table: technologies_productstructuretreenode

-- changed: 29.05.2014

CREATE TABLE technologies_productstructuretreenode
(
  id bigint NOT NULL,
  priority integer,
  technology_id bigint,
  "number" character varying(255),
  product_id bigint,
  createDate timestamp,
  updateDate timestamp,
  createUser character varying(255),
  updateUser character varying(255),
  CONSTRAINT technologies_productstructuretreenode_pkey PRIMARY KEY (id),
  CONSTRAINT productstructuretreenode_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT productstructuretreenode_technology_fkey FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) DEFERRABLE
);