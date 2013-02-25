-- Table: materialflow_transfer
-- changed: 25.02.2013

ALTER TABLE materialflow_transfer ADD COLUMN fromdelivery_id bigint;

ALTER TABLE materialflow_transfer
  ADD CONSTRAINT transfer_delivery_fkey FOREIGN KEY (fromdelivery_id)
      REFERENCES deliveries_delivery (id) DEFERRABLE;

-- end
