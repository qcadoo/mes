-- #407 begin - States of technology

ALTER TABLE technologies_technology ADD COLUMN state varchar(255) DEFAULT 'draft';

BEGIN;
UPDATE technologies_technology SET state = 'accepted' WHERE id IN (SELECT technology_id FROM orders_order) AND state = 'draft';
COMMIT;

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
ALTER TABLE technologies_logging;

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
ALTER TABLE simplematerialbalance_simplematerialbalance;

-- end
--  TABLE Simple material balance orders component

CREATE TABLE simplematerialbalance_simplematerialbalanceorderscomponent
(
  id bigint NOT NULL,
  simplematerialbalance_id bigint,
  order_id bigint,
  CONSTRAINT simplematerialbalance_simplematerialbalanceorderscomponent_pkey PRIMARY KEY (id)  
)
WITH (
  OIDS=FALSE
);
ALTER TABLE simplematerialbalance_simplematerialbalanceorderscomponent;

-- end

--  TABLE Simple material balances stock areas component

CREATE TABLE simplematerialbalance_simplematerialbalancestockareascomponent
(
  id bigint NOT NULL,
  simplematerialbalance_id bigint,
  stockareas_id bigint,
  CONSTRAINT simplematerialbalance_simplematerialbalancestockareascomponent_pkey PRIMARY KEY (id) 
)
WITH (
  OIDS=FALSE
);
ALTER TABLE simplematerialbalance_simplematerialbalancestockareascomponent;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN qualityControlRequired boolean ;
--end

