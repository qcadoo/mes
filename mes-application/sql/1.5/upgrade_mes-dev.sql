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