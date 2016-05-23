-- added set to product components
-- last touched 30.03.2016 by kama

ALTER TABLE technologies_operationproductoutcomponent ADD COLUMN set boolean;
ALTER TABLE technologies_operationproductoutcomponent ALTER COLUMN set SET DEFAULT false;

-- end

-- sets tables
-- last touched 6.04.2016 by pako

CREATE TABLE productioncounting_settechnologyincomponents
(
  id bigint NOT NULL,
  trackingoperationproductincomponent_id bigint,
  product_id bigint,
  quantityfromsets numeric(12,5),
  CONSTRAINT productioncounting_settechnologyincomponents_pkey PRIMARY KEY (id),
  CONSTRAINT settechnologyic_trackingoperationproductic_fkey FOREIGN KEY (trackingoperationproductincomponent_id)
      REFERENCES productioncounting_trackingoperationproductincomponent (id) DEFERRABLE,
  CONSTRAINT settechnologyic_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

CREATE TABLE productioncounting_settrackingoperationproductincomponents
(
  id bigint NOT NULL,
  trackingoperationproductoutcomponent_id bigint,
  product_id bigint,
  quantityfromsets numeric(12,5),
  CONSTRAINT productioncounting_settrackingoperationproductincomponents_pkey PRIMARY KEY (id),
  CONSTRAINT settrackingoperationproductic_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT settrackingoperationproductic_trackingoperationproductoc_fkey FOREIGN KEY (trackingoperationproductoutcomponent_id)
      REFERENCES productioncounting_trackingoperationproductoutcomponent (id) DEFERRABLE
);

-- end

-- NBLS-181
alter table basicproductioncounting_productionCountingQuantity add column set character varying(255);
--end

-- another sets tables
-- last touched 14.04.2016 by pako

CREATE TABLE productioncounting_productioncountingquantitysetcomponent
(
  id bigint NOT NULL,
  productioncountingquantity_id bigint,
  product_id bigint,
  quantityfromsets numeric(12,5),
  plannedquantityfromproduct numeric(12,5),
  outquantity numeric(12,5),
  CONSTRAINT productioncounting_productioncountingquantitysetcomponent_pkey PRIMARY KEY (id),
  CONSTRAINT productioncountingquantitysc_productioncountingquantity_fkey FOREIGN KEY (productioncountingquantity_id)
      REFERENCES basicproductioncounting_productioncountingquantity (id) DEFERRABLE,
  CONSTRAINT productioncountingquantitysc_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE
);

-- end


-- #NBLS-160

ALTER TABLE basic_parameter ADD COLUMN consumptionofrawmaterialsbasedonstandards boolean DEFAULT false;

-- end


-- Added new role for production registration terminal
-- last touched 01.04.2016 by lupo

SELECT add_role('ROLE_PRODUCTION_REGISTRATION_TERMINAL','Dostęp do terminalu rejestracji produkcji');
SELECT add_group_role('SUPER_ADMIN', 'ROLE_PRODUCTION_REGISTRATION_TERMINAL');
SELECT add_group_role('ADMIN', 'ROLE_PRODUCTION_REGISTRATION_TERMINAL');
SELECT add_group_role('USER', 'ROLE_PRODUCTION_REGISTRATION_TERMINAL');

-- end
