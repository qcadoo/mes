-- Table: basic_country
-- changed: 24.04.2013

CREATE TABLE basic_country
(
  id bigint NOT NULL,
  code character varying(2),
  country character varying(255),
  CONSTRAINT basic_country_pkey PRIMARY KEY (id)
);

-- end


-- Table: basic_parameter
-- changed: 24.04.2013

ALTER TABLE basic_parameter ADD COLUMN country_id bigint;

ALTER TABLE basic_parameter
  ADD CONSTRAINT parameter_country_fkey FOREIGN KEY (country_id)
      REFERENCES basic_country (id) DEFERRABLE;
      
-- end


-- Table: basic_company
-- changed: 24.04.2013

ALTER TABLE basic_company ADD COLUMN country_id bigint;

ALTER TABLE basic_company
  ADD CONSTRAINT company_country_fkey FOREIGN KEY (country_id)
      REFERENCES basic_country (id) DEFERRABLE;
      
ALTER TABLE basic_company ADD COLUMN taxcountrycode_id bigint;      

ALTER TABLE basic_company
  ADD CONSTRAINT company_taxtcountrycode_fkey FOREIGN KEY (taxcountrycode_id)
      REFERENCES basic_country (id) DEFERRABLE;
      
-- end
