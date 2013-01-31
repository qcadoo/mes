-- Table: deliveries_delivery
-- changed: 31.01.2013

ALTER TABLE deliveries_delivery ADD COLUMN active boolean;
ALTER TABLE deliveries_delivery ALTER COLUMN active SET DEFAULT true;
UPDATE deliveries_delivery SET active = true;

-- end