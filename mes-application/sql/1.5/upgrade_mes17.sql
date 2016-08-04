-- rename fields in settings
-- last touched 2.06.2016 by pako

UPDATE materialflowresources_documentpositionparametersitem SET name = 'productionDate' WHERE name = 'productiondate';
UPDATE materialflowresources_documentpositionparametersitem SET name = 'expirationDate' WHERE name = 'expirationdate';

-- end


-- resource stock view
-- last touched 30.05.2016 by kama

CREATE TABLE materialflowresources_resourcestock
(
  id bigint NOT NULL,
  location_id bigint,
  product_id bigint,
  quantity numeric(12,5),
  reservedquantity numeric(12,5),
  availablequantity numeric(12,5),
  CONSTRAINT materialflowresources_resourcestock_pkey PRIMARY KEY (id),
  CONSTRAINT resourcestock_product_fkey FOREIGN KEY (product_id)
      REFERENCES basic_product (id) DEFERRABLE,
  CONSTRAINT resourcestock_location_fkey FOREIGN KEY (location_id)
      REFERENCES materialflow_location (id) DEFERRABLE
);

CREATE SEQUENCE materialFlowResources_resourcestockdto_id_seq;

CREATE OR REPLACE VIEW materialflowresources_orderedquantitystock AS
    SELECT COALESCE(SUM(orderedproduct.orderedquantity), 0::numeric) AS orderedquantity,
    resource.id AS resource_id
    FROM materialflowresources_resourcestock resource
    JOIN deliveries_orderedproduct orderedproduct ON (orderedproduct.product_id = resource.product_id)
    JOIN deliveries_delivery delivery ON (orderedproduct.delivery_id = delivery.id AND delivery.active = true
        AND delivery.location_id = resource.location_id
        AND (delivery.state::text = ANY (ARRAY['01draft'::character varying::text, '02prepared'::character varying::text, '03duringCorrection'::character varying::text, '05approved'::character varying::text])))
    GROUP BY resource.id;

CREATE OR REPLACE VIEW materialflowresources_resourcestockdto_internal AS
    SELECT row_number() OVER () AS id, resource.location_id, resource.product_id::integer, resource.quantity AS quantity,
    COALESCE(orderedquantity.orderedquantity, 0::numeric) AS orderedquantity,
    (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum
        FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id
            AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id) AS minimumstate,
    reservedQuantity, availableQuantity
    FROM materialflowresources_resourcestock resource
    LEFT JOIN materialflowresources_orderedquantitystock orderedquantity ON (orderedquantity.resource_id = resource.id)
    GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity, reservedQuantity, availableQuantity, quantity;

CREATE OR REPLACE VIEW materialflowresources_resourcestockdto AS
    SELECT internal.*, location.number AS locationNumber, location.name AS locationName, product.number AS productNumber,
    product.name AS productName, product.unit AS productUnit
    FROM materialflowresources_resourcestockdto_internal internal
    JOIN materialflow_location location ON (location.id = internal.location_id)
    JOIN basic_product product ON (product.id = internal.product_id);

-- end


-- TABLE cmmsmachineparts_maintenanceevent
-- last touched 23.06.2016 by kasi

ALTER TABLE cmmsmachineparts_maintenanceevent ADD COLUMN soundnotifications boolean;
ALTER TABLE cmmsmachineparts_maintenanceevent ALTER COLUMN soundnotifications SET DEFAULT false;

-- end


SELECT add_role('ROLE_EVENTS_NOTIFICATION','Powiadomnienia o zdarzeniach (awariach/problemach) o statusie nowe');