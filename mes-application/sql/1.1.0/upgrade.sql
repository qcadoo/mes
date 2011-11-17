-- #407 begin - States of technology

ALTER TABLE technologies_technology ADD COLUMN state varchar(255) DEFAULT 'draft';

CREATE TABLE technologies_logging (
	id bigint NOT NULL,
  technology_id bigint,
  dateandtime timestamp without time zone,
  previousstate character varying(255),
  currentstate character varying(255),
  shift_id bigint,
  worker character varying(255),
  active boolean DEFAULT true,
  CONSTRAINT technologies_logging_pkey PRIMARY KEY (id),
  CONSTRAINT technologies_logging_fkey_shift FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT technologies_logging_fkey_technology FOREIGN KEY (technology_id)
      REFERENCES technologies_technology (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE technologies_logging OWNER TO postgres;

-- #407 end

--  TABLE Simple material balance;

CREATE TABLE simplematerialbalance_simplematerialbalance
(
  id bigint NOT NULL,
  "name" character varying(255),
  date timestamp without time zone,
  worker character varying(255),
  onlycomponents boolean DEFAULT true,
  generated boolean,
  filename character varying(255),
  CONSTRAINT simplematerialbalance_simplematerialbalance_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE simplematerialbalance_simplematerialbalance OWNER TO postgres;

-- end
--  TABLE Simple material balance orders component

CREATE TABLE simplematerialbalance_simplematerialbalanceorderscomponent
(
  id bigint NOT NULL,
  simplematerialbalance_id bigint,
  order_id bigint,
  CONSTRAINT simplematerialbalance_simplematerialbalanceorderscomponent_pkey PRIMARY KEY (id),
  CONSTRAINT simplematerialbalance_simplematerialbalanceorderscomponent_fkey_simplematerialbalance FOREIGN KEY (simplematerialbalance_id)
      REFERENCES simplematerialbalance_simplematerialbalance (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT simplematerialbalance_simplematerialbalanceorderscomponent_fkey_order FOREIGN KEY (order_id)
      REFERENCES orders_order (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE simplematerialbalance_simplematerialbalanceorderscomponent OWNER TO postgres;

-- end

--  TABLE Simple material balances stock areas component

CREATE TABLE simplematerialbalance_simplematerialbalancestockareascomponent
(
  id bigint NOT NULL,
  simplematerialbalance_id bigint,
  stockareas_id bigint,
  CONSTRAINT simplematerialbalance_simplematerialbalancestockareascompo_pkey PRIMARY KEY (id),
  CONSTRAINT simplematerialbalance_simplematerialbalancestockareascompo_fkey_stockareas FOREIGN KEY (stockareas_id)
      REFERENCES materialflow_stockareas (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT simplematerialbalance_simplematerialbalancestockareascompo_fkey_simplematerialbalance FOREIGN KEY (simplematerialbalance_id)
      REFERENCES simplematerialbalance_simplematerialbalance (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE simplematerialbalance_simplematerialbalancestockareascomponent OWNER TO postgres;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN qualityControlRequired boolean ;
--end

