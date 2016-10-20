-- Added new parameter rr
-- last touched 28.09.2016 by kasi

ALTER TABLE basic_parameter ADD COLUMN autorecalculateorder boolean;

-- end

-- added pps report
-- last touched 06.10.2016 by kama

CREATE TABLE productionpershift_ppsreport
(
  id bigint NOT NULL,
  "number" character varying(1024),
  name character varying(1024),
  datefrom date,
  dateto date,
  filename character varying(255),
  generated boolean DEFAULT false,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT productionpershift_ppsreport_pkey PRIMARY KEY (id)
);

-- end
