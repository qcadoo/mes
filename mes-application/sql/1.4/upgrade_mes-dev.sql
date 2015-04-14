-- Added unit conversions to OPIC
-- last touched 14.04.2015 by kama

ALTER TABLE technologies_operationproductincomponent ADD COLUMN givenquantity numeric(14,5);
ALTER TABLE technologies_operationproductincomponent ADD COLUMN givenunit character varying(255);

-- end
