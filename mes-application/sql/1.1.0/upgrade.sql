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