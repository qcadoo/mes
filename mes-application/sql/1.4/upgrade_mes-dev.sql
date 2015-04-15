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
