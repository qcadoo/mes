-- QCADOOCLS-4315

CREATE OR REPLACE VIEW materialflowresources_orderedquantity AS
    SELECT
        COALESCE(SUM(orderedproduct.orderedquantity), 0::numeric) AS orderedquantity,
        resource.id AS resource_id
    FROM materialflowresources_resource resource
    JOIN deliveries_orderedproduct orderedproduct
        ON (orderedproduct.product_id = resource.product_id)
    JOIN deliveries_delivery delivery
        ON (orderedproduct.delivery_id = delivery.id
            AND delivery.active = true
            AND delivery.location_id = resource.location_id
            AND (delivery.state::text = ANY (ARRAY['01draft'::character varying::text, '02prepared'::character varying::text, '03duringCorrection'::character varying::text, '05approved'::character varying::text])))
    GROUP BY resource.id;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto_internal AS
    SELECT row_number() OVER () AS id,
        resource.location_id,
        resource.product_id::integer,
        SUM(resource.quantity) AS quantity,
        COALESCE(orderedquantity.orderedquantity, 0::numeric) AS orderedquantity,
        (
            SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum
            FROM warehouseminimalstate_warehouseminimumstate
            WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id
                AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id
        ) AS minimumstate
    FROM materialflowresources_resource resource
    LEFT JOIN materialflowresources_orderedquantity orderedquantity
        ON (orderedquantity.resource_id = resource.id)
    GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity;

CREATE SEQUENCE materialflowresources_warehousestocklistdto_id_seq;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto AS
    SELECT
        internal.*,
        location.number AS locationNumber,
        location.name AS locationName,
        product.number AS productNumber,
        product.name AS productName,
        product.unit AS productUnit
	FROM materialflowresources_warehousestocklistdto_internal internal
	JOIN materialflow_location location
	    ON (location.id = internal.location_id)
	JOIN basic_product product
	    ON (product.id = internal.product_id);

-- end
