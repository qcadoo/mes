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


-- Table: deliveries_parameterdeliveryordercolumn
-- changed: 09.05.2013

CREATE TABLE deliveries_parameterdeliveryordercolumn
(
  id bigint NOT NULL,
  parameter_id bigint,
  columnfororders_id bigint,
  succession integer,
  CONSTRAINT deliveries_parameterdeliveryordercolumn_pkey PRIMARY KEY (id),
  CONSTRAINT parameterdeliveryordercolumn_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) DEFERRABLE,
  CONSTRAINT parameterdeliveryordercolumn_columnfororders_fkey FOREIGN KEY (columnfororders_id)
      REFERENCES deliveries_columnfororders (id) DEFERRABLE
);

-- end
