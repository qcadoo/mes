-- Table: basic_parameter
-- changed: 21.05.2012

ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenCorrectingDateFrom TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenCorrecringDateFrom SET DEFAULT false;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenCorrectingDateTo TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenCorrectingDateTo SET DEFAULT false;

ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenChangingStateToDeclined TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenChangingStateToDeclined SET DEFAULT false;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenChangingStateToInterrupted TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenChangingStateToInterrupted SET DEFAULT false;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenChangingStateToAbandoned TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenChangingStateToAbandoned SET DEFAULT false;

ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenDelayedEffectiveDateFrom TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenDelayedEffectiveDateFrom SET DEFAULT false;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenEarliedEffectiveDateFrom  TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenEarliedEffectiveDateFrom SET DEFAULT false;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenDelayedEffectiveDateTo TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenDelayedEffectiveDateTo SET DEFAULT false;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenEarliedEffectiveDateTo  TYPE boolean;
ALTER TABLE basic_parameter ALTER COLUMN reasonNeededWhenEarliedEffectiveDateTo SET DEFAULT false;

ALTER TABLE basic_parameter ALTER COLUMN delayedEffectiveDateFrom  TYPE integer;
ALTER TABLE basic_parameter ALTER COLUMN earliedEffectiveDateFrom TYPE integer;
ALTER TABLE basic_parameter ALTER COLUMN delayedEffectiveDateTo TYPE integer;
ALTER TABLE basic_parameter ALTER COLUMN earliedEffectiveDateTo TYPE integer;

-- end

-- table: orders_order
-- changed: 17.05.2012

ALTER TABLE orders_order ADD COLUMN correcteddatefrom timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN correcteddateto timestamp without time zone;
ALTER TABLE orders_order ADD COLUMN reasontypecorrectiondatefrom character varying(255);
ALTER TABLE orders_order ADD COLUMN reasontypecorrectiondateto character varying(255);
ALTER TABLE orders_order ADD COLUMN commentreasontypecorrectiondatefrom character varying(255);
ALTER TABLE orders_order ADD COLUMN commentreasontypecorrectiondateto character varying(255);

-- end

-- table: orders_logging
-- changed: 21.05.2012

ALTER TABLE orders_logging ADD COLUMN reasontype character varying(255);
ALTER TABLE orders_logging ADD COLUMN "comment" character varying(255);

-- end

CREATE TABLE linechangeovernorms_linechangeovernorms
(
  id bigint NOT NULL,
  "number" character varying(255),
  changeovertype character varying(255) DEFAULT '01fromTechForSpecificLine'::character varying,
  fromtechnology_id bigint,
  totechnology_id bigint,
  fromtechnologygroup_id bigint,
  totechnologygroup_id bigint,
  productionline_id bigint,
  duration integer,
  CONSTRAINT linechangeovernorms_linechangeovernorms_pkey PRIMARY KEY (id),
  CONSTRAINT fk95403ebf63154a1c FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT linechangeovernorms_technology_fkey FOREIGN KEY (totechnology_id)
      REFERENCES technologies_technology (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT linechangeovernorms_technology_fkey FOREIGN KEY (fromtechnology_id)
      REFERENCES technologies_technology (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT linechangeovernorms_technologyGroup_fkey FOREIGN KEY (fromtechnologygroup_id)
      REFERENCES technologies_technologygroup (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT linechangeovernorms_technologyGroup_fkey FOREIGN KEY (totechnologygroup_id)
      REFERENCES technologies_technologygroup (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE linechangeovernorms_linechangeovernorms OWNER TO postgres;

-- Table: productionpershift_productionpershift

-- DROP TABLE productionpershift_productionpershift;

CREATE TABLE productionpershift_productionpershift
(
  id bigint NOT NULL,
  order_id bigint,
  plannedprogresscorrectiontype character varying(255),
  plannedprogresscorrectioncomment text,
  technologyinstanceoperationcomponent_id bigint,
  CONSTRAINT productionpershift_productionpershift_pkey PRIMARY KEY (id),
  CONSTRAINT productionpershift_tioc_fkey FOREIGN KEY (technologyinstanceoperationcomponent_id)
      REFERENCES technologies_technologyinstanceoperationcomponent (id) DEFERRABLE,
  CONSTRAINT productionpershift_order_fkey FOREIGN KEY (order_id)
      REFERENCES orders_order (id) DEFERRABLE
);

-- Table: productionpershift_progressforday

-- DROP TABLE productionpershift_progressforday;

CREATE TABLE productionpershift_progressforday
(
  id bigint NOT NULL,
  technologyinstanceoperationcomponent_id bigint,
  "day" integer,
  CONSTRAINT productionpershift_progressforday_pkey PRIMARY KEY (id),
  CONSTRAINT progressforday_tioc_fkey FOREIGN KEY (technologyinstanceoperationcomponent_id)
      REFERENCES technologies_technologyinstanceoperationcomponent (id) DEFERRABLE
);

-- Table: productionpershift_dailyprogress

-- DROP TABLE productionpershift_dailyprogress;

CREATE TABLE productionpershift_dailyprogress
(
  id bigint NOT NULL,
  progressforday_id bigint,
  shift_id bigint,
  quantity numeric(10,3),
  CONSTRAINT productionpershift_dailyprogress_pkey PRIMARY KEY (id),
  CONSTRAINT dailyprogress_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE,
  CONSTRAINT dailyprogress_progressforday_fkey FOREIGN KEY (progressforday_id)
      REFERENCES productionpershift_progressforday (id) DEFERRABLE
);