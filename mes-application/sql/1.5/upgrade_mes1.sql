-- Changes in cost calculation
-- last touched 25.09.2015 by pako

ALTER TABLE basic_parameter ADD COLUMN profitpb numeric(12,5);
ALTER TABLE basic_parameter ADD COLUMN registrationpriceoverheadpb numeric(12,5);
ALTER TABLE basic_parameter ADD COLUMN sourceofoperationcostspb character varying(255);
ALTER TABLE costcalculation_costcalculation ADD COLUMN sourceofoperationcosts character varying(255);
ALTER TABLE costcalculation_costcalculation ADD COLUMN registrationpriceoverhead numeric(19,5);
ALTER TABLE costcalculation_costcalculation ADD COLUMN profit numeric(19,5);
ALTER TABLE costcalculation_costcalculation ADD COLUMN registrationpriceoverheadvalue numeric(19,5);
ALTER TABLE costcalculation_costcalculation ADD COLUMN profitvalue numeric(19,5);
ALTER TABLE costcalculation_costcalculation ADD COLUMN sellpricevalue numeric(19,5);

-- end


-- Changes in products (additional codes, unit)
-- last touched 07.10.2015 by kama

ALTER TABLE basic_product ADD COLUMN additionalunit character varying(255);

CREATE TABLE basic_additionalcode
(
  id bigint NOT NULL,
  code character varying(255),
  product_id bigint,
  CONSTRAINT basic_additionalcode_pkey PRIMARY KEY (id),
  CONSTRAINT additionalcode_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end


CREATE TABLE materialflowresources_storagelocation
(
  id bigint NOT NULL,
  "number" character varying(1024),
  state character varying(255) DEFAULT '01draft'::character varying,
  location_id bigint,
  product_id bigint,
  placestoragelocation boolean DEFAULT false,
  maximumnumberofpallets numeric(12,5),
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT materialflowresources_storagelocation_pkey PRIMARY KEY (id),
  CONSTRAINT storagelocation_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT storagelocation_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

CREATE TABLE materialflowresources_storagelocationhelper
(
  id bigint NOT NULL,
  prefix character varying(255),
  "number" character varying(255),
  location_id bigint,
  placestoragelocation boolean DEFAULT false,
  numberofstoragelocations numeric(12,5),
  maximumnumberofpallets numeric(12,5),
  CONSTRAINT materialflowresources_storagelocationhelper_pkey PRIMARY KEY (id),
  CONSTRAINT storagelocationhelper_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);
