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


-- Table: technologies_technologyoperationcomponent
-- changed: 22.06.2013

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN createdate timestamp without time zone;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN updatedate timestamp without time zone;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN createuser character varying(255);
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN updateuser character varying(255);

-- end


-- Table: timenormsforoperations_techopercomptimecalculations
-- changed: 22.06.2013

CREATE TABLE timenormsforoperations_techopercomptimecalculations
(
  id bigint NOT NULL,
  operationoffset integer,
  effectiveoperationrealizationtime integer,
  effectivedatefrom timestamp without time zone,
  effectivedateto timestamp without time zone,
  duration integer DEFAULT 0,
  machineworktime integer DEFAULT 0,
  laborworktime integer DEFAULT 0,
  CONSTRAINT timenormsforoperations_techopercomptimecalculations_pkey PRIMARY KEY (id)
);

-- end


-- Table: productionlines_techopercompworkstation
-- changed: 22.06.2013

CREATE TABLE productionlines_techopercompworkstation
(
  id bigint NOT NULL,
  quantityofworkstationtypes integer DEFAULT 1,
  CONSTRAINT productionlines_techopercompworkstation_pkey PRIMARY KEY (id)
);

-- end


-- Table: operationaltasksfororders_techopercompoperationaltasks
-- changed: 22.06.2013

CREATE TABLE operationaltasksfororders_techopercompoperationaltasks
(
  id bigint NOT NULL,
  technologyoperationcomponent_id bigint,
  CONSTRAINT operationaltasksfororders_techopercompoperationaltasks_pkey PRIMARY KEY (id),
  CONSTRAINT techopercompoperationaltasks_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE
);

-- end


-- Table: technologies_technologyoperationcomponent
-- changed: 22.06.2013

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN techopercomptimecalculations_id bigint;      

ALTER TABLE technologies_technologyoperationcomponent
  ADD CONSTRAINT technologyoperationcomponent_techopercomptimecalculations_fkey FOREIGN KEY (techopercomptimecalculations_id)
      REFERENCES timenormsforoperations_techopercomptimecalculations (id) DEFERRABLE;


ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN techopercompworkstation_id bigint;      

ALTER TABLE technologies_technologyoperationcomponent
  ADD CONSTRAINT technologyoperationcomponent_techopercompworkstation_fkey FOREIGN KEY (techopercompworkstation_id)
      REFERENCES productionlines_techopercompworkstation (id) DEFERRABLE;

-- end