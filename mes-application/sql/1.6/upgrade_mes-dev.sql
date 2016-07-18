-- reservations
-- last touched 28.06.2016 by kama

CREATE TABLE materialflowresources_reservation
(
  id bigint NOT NULL,
  location_id bigint,
  product_id bigint,
  quantity numeric(12,5),
  position_id bigint,
  CONSTRAINT materialflowresources_reservation_pkey PRIMARY KEY (id),
  CONSTRAINT reservation_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT reservation_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT reservation_position_fkey FOREIGN KEY (position_id)
      REFERENCES materialflowresources_position (id) DEFERRABLE
);

ALTER TABLE materialflowresources_documentpositionparameters ADD COLUMN draftmakesreservation boolean DEFAULT true;

-- end


-- deliveries changes
-- last touched 20.06.2016 by pako

ALTER TABLE deliveries_deliveredproduct ADD COLUMN additionalquantity numeric(12,5);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN conversion numeric(12,5);
ALTER TABLE deliveries_deliveredproduct ADD COLUMN iswaste boolean;
ALTER TABLE deliveries_deliveredproduct ADD COLUMN additionalunit character varying(255);

CREATE TABLE deliveries_deliveredproductmulti
(
  id bigint NOT NULL,
  delivery_id bigint,
  palletnumber_id bigint,
  pallettype character varying(255),
  storagelocation_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT deliveries_deliveredproductmulti_pkey PRIMARY KEY (id),
  CONSTRAINT deliveredproductmulti_delivery_fkey FOREIGN KEY (delivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE,
  CONSTRAINT deliveredproductmulti_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE,
  CONSTRAINT deliveredproductmulti_storagelocation_fkey FOREIGN KEY (storagelocation_id)
      REFERENCES materialflowresources_storagelocation (id) DEFERRABLE
);

CREATE TABLE deliveries_deliveredproductmultiposition
(
  id bigint NOT NULL,
  deliveredproductmulti_id bigint,
  product_id bigint,
  quantity numeric(12,5),
  additionalquantity numeric(12,5),
  conversion numeric(12,5),
  iswaste boolean,
  expirationdate date,
  unit character varying(255),
  additionalunit character varying(255),
  additionalcode_id bigint,
  CONSTRAINT deliveries_deliveredproductmultiposition_pkey PRIMARY KEY (id),
  CONSTRAINT deliveredproductmp_deliveredproductmulti_fkey FOREIGN KEY (deliveredproductmulti_id)
      REFERENCES deliveries_deliveredproductmulti (id) DEFERRABLE,
  CONSTRAINT deliveredproductmp_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT deliveredproductmp_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE
);

-- end
