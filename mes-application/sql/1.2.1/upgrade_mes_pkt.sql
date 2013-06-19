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
