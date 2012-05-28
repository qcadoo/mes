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


-- table: linechangeovernorms_linechangeovernorms
-- changed: 25.05.2012

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
  CONSTRAINT linechangeovernorms_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT linechangeovernorms_fromtechnology_fkey FOREIGN KEY (fromtechnology_id)
      REFERENCES technologies_technology (id) DEFERRABLE,
  CONSTRAINT linechangeovernorms_totechnology_fkey FOREIGN KEY (totechnology_id)
      REFERENCES technologies_technology (id) DEFERRABLE,
  CONSTRAINT linechangeovernorms_fromtechnologygroup_fkey FOREIGN KEY (fromtechnologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE,
  CONSTRAINT linechangeovernorms_totechnologygroup_fkey FOREIGN KEY (totechnologygroup_id)
      REFERENCES technologies_technologygroup (id) DEFERRABLE
);

-- end
 

-- table: orders_order
-- changed: 25.05.2012

ALTER TABLE orders_order ALTER COLUMN ownlinechangeover  TYPE boolean;
ALTER TABLE orders_order ALTER COLUMN ownlinechangeover SET DEFAULT false;

ALTER TABLE orders_order ALTER COLUMN ownlinechangeoverduration TYPE integer;

-- end
