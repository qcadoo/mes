-- Table: basic_reportcolumnwidth
-- changed: 11.02.2013

CREATE TABLE basic_reportcolumnwidth
(
  id bigint NOT NULL,
  identifier character varying(255),
  name character varying(1024),
  width integer,
  chartype character varying(255),
  parameter_id bigint,
  CONSTRAINT basic_reportcolumnwidth_pkey PRIMARY KEY (id),
  CONSTRAINT basic_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) deferrable
);

--end
