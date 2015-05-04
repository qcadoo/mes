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

-- Added factories (DUR)
-- last touched 27.04.2015 by kama 

CREATE TABLE basic_factory
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(1024),
  city character varying(255),
  active boolean DEFAULT true,
  CONSTRAINT basic_factory_pkey PRIMARY KEY (id)
);

ALTER TABLE basic_division ADD COLUMN factory_id bigint;
ALTER TABLE basic_division
  ADD CONSTRAINT division_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE;

-- end