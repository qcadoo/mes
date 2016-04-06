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