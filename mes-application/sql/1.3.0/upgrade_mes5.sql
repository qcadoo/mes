--  Add 'description' field to the warehouse document models.
-- last touched at 26.08.2014 by tola

ALTER TABLE materialflowresources_document ADD COLUMN description character varying(2048);

--  Update 'zipcode' field type in company model.
-- last touched at 26.08.2014 by tola

ALTER TABLE basic_company ALTER COLUMN zipcode TYPE character varying(255);

--  Added help tables for merging operations due grouping
-- last touched at 04.09.2014 by adso

CREATE TABLE technologies_technologyoperationcomponentmergeproductin
(
  id bigint NOT NULL,
  operationcomponent_id bigint,
  mergedoperationcomponent_id bigint,
  mergedoperationproductcomponent_id bigint,
  quantitychange numeric(12,5),
  order_id bigint,
  active boolean DEFAULT true,
  CONSTRAINT technologies_technologyoperationcomponentmergeproductin_pkey PRIMARY KEY (id),
  CONSTRAINT fk15b8c3f3883685ef FOREIGN KEY (mergedoperationproductcomponent_id)
      REFERENCES technologies_operationproductincomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk15b8c3f3a29938f8 FOREIGN KEY (operationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk15b8c3f3b64bada8 FOREIGN KEY (order_id)
      REFERENCES orders_order (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk15b8c3f3d8cc494c FOREIGN KEY (mergedoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE technologies_technologyoperationcomponentmergeproductout
(
  id bigint NOT NULL,
  operationcomponent_id bigint,
  mergedoperationcomponent_id bigint,
  mergedoperationproductcomponent_id bigint,
  quantitychange numeric(12,5),
  order_id bigint,
  active boolean DEFAULT true,
  CONSTRAINT technologies_technologyoperationcomponentmergeproductout_pkey PRIMARY KEY (id),
  CONSTRAINT fka15fd2408ab225a FOREIGN KEY (mergedoperationproductcomponent_id)
      REFERENCES technologies_operationproductoutcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fka15fd240a29938f8 FOREIGN KEY (operationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fka15fd240b64bada8 FOREIGN KEY (order_id)
      REFERENCES orders_order (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fka15fd240d8cc494c FOREIGN KEY (mergedoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);