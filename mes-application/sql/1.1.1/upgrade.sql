-- Table: basic_manufacturer

-- added 28.11.2011

CREATE TABLE basic_manufacturer
(
  id bigint NOT NULL,
  companyfullname character varying(255) DEFAULT 'Company'::character varying,
  tax character varying(30),
  street character varying(255),
  house character varying(30),
  flat character varying(30),
  zipcode character varying(30),
  city character varying(255),
  state character varying(30),
  country character varying(30),
  email character varying(30),
  addresswww character varying(30),
  phone character varying(25),
  active boolean DEFAULT true,
  CONSTRAINT basic_manufacturer_pkey PRIMARY KEY (id )
)
WITH (
  OIDS=FALSE
);

-- end