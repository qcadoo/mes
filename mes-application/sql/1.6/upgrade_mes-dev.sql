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