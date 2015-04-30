-- Added unit conversions to OPIC, positions and products in RR
-- last touched 14.04.2015 by kama

ALTER TABLE technologies_operationproductincomponent ADD COLUMN givenquantity numeric(14,5);
ALTER TABLE technologies_operationproductincomponent ADD COLUMN givenunit character varying(255);

ALTER TABLE materialflowresources_position ADD COLUMN givenunit character varying(255);
ALTER TABLE materialflowresources_position ADD COLUMN givenquantity numeric(14,5);

ALTER TABLE productioncounting_trackingoperationproductincomponent ADD COLUMN givenunit character varying(255);
ALTER TABLE productioncounting_trackingoperationproductincomponent ADD COLUMN givenquantity numeric(14,5);

ALTER TABLE productioncounting_trackingoperationproductoutcomponent ADD COLUMN givenunit character varying(255);
ALTER TABLE productioncounting_trackingoperationproductoutcomponent ADD COLUMN givenquantity numeric(14,5);

-- end


-- table: masterorders_masterorderdefinition
-- last touched 22.04.2015 by lupo

CREATE TABLE masterorders_masterorderdefinition
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(1024),
  parameter_id bigint,
  active boolean DEFAULT true,
  CONSTRAINT masterorders_masterorderdefinition_pkey PRIMARY KEY (id),
  CONSTRAINT masterorderdefinition_parameter_fkey FOREIGN KEY (parameter_id)
      REFERENCES basic_parameter (id) DEFERRABLE
);

-- end


-- Table: masterorders_masterorder
-- last touched 22.04.2015 by lupo

ALTER TABLE masterorders_masterorder ADD COLUMN masterorderdefinition_id bigint;

ALTER TABLE masterorders_masterorder
  ADD CONSTRAINT masterorder_masterorderdefinition_fkey FOREIGN KEY (masterorderdefinition_id)
      REFERENCES masterorders_masterorderdefinition (id) DEFERRABLE;

ALTER TABLE masterorders_masterorder ADD COLUMN startdate timestamp without time zone;
ALTER TABLE masterorders_masterorder ADD COLUMN finishdate timestamp without time zone;

-- end
