-- last touched 24.05.2016 by kama

ALTER TABLE materialflowresources_resource ADD COLUMN username character varying(255);

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

CREATE OR REPLACE VIEW materialflowresources_resourcestockdto AS
    SELECT row_number() OVER () AS id, product_id, location_id,
    product.name AS productName, product.number AS productNumber, product.unit AS productUnit,
    location.number AS locationNumber, location.name AS locationName,
    (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate
        WHERE warehouseminimalstate_warehouseminimumstate.product_id = stock.product_id
            AND warehouseminimalstate_warehouseminimumstate.location_id = stock.location_id) AS minimumstate,
    ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity),0) AS sum
        FROM deliveries_orderedproduct, deliveries_delivery
        WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id
            AND deliveries_delivery.active = true AND deliveries_delivery.location_id = stock.location_id
            AND deliveries_orderedproduct.product_id = stock.product_id
            AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity,
    quantity, reservedQuantity, availableQuantity
    FROM materialflowresources_resourcestock stock
    LEFT JOIN basic_product product ON stock.product_id = product.id
    LEFT JOIN materialflow_location location ON stock.location_id = location.id;

-- end