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


-- Table: orders_order
-- changed: 20.05.2013

ALTER TABLE orders_order ADD COLUMN  ordertype character varying(255) DEFAULT '01withPatternTechnology'::character varying;

-- end


-- Table: technologies_technology
-- changed: 05.06.2013

ALTER TABLE technologies_technology ADD COLUMN  technologytype character varying(255);

-- end


-- Table: technologies_technology
-- changed: 05.06.2013

ALTER TABLE technologies_technology ADD COLUMN  technologyprototype_id bigint;

ALTER TABLE technologies_technology
  ADD CONSTRAINT technology_technology_fkey FOREIGN KEY (technologyprototype_id)
      REFERENCES technologies_technology (id) DEFERRABLE;
      
-- end

      
-- Table: orders_order
-- changed: 05.06.2013

ALTER TABLE orders_order ADD COLUMN  technologyprototype_id bigint;

ALTER TABLE orders_order
  ADD CONSTRAINT order_technology_fkey FOREIGN KEY (technologyprototype_id)
      REFERENCES technologies_technology (id) DEFERRABLE;
    
-- end


-- Table: basicproductioncounting_productioncountingquantity
-- changed: 04.06.2013

ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN technologyoperationcomponent_id bigint;

ALTER TABLE basicproductioncounting_productioncountingquantity
  ADD CONSTRAINT productioncountingquantity_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;


ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN basicproductioncounting_id bigint;

ALTER TABLE basicproductioncounting_productioncountingquantity
  ADD CONSTRAINT productioncountingquantity_basicproductioncounting_fkey FOREIGN KEY (basicproductioncounting_id)
      REFERENCES basicproductioncounting_basicproductioncounting (id) DEFERRABLE;
      
ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN typeofmaterial character varying(255);
ALTER TABLE basicproductioncounting_productioncountingquantity ALTER COLUMN typeofmaterial SET DEFAULT '01component'::character varying;

ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN role character varying(255);
ALTER TABLE basicproductioncounting_productioncountingquantity ALTER COLUMN role SET DEFAULT '01used'::character varying;
     
-- end
