

-- QCADOOCLS-4315
CREATE OR REPLACE VIEW materialflowresources_orderedquantity AS
 SELECT COALESCE(sum(orderedproduct.orderedquantity), 0::numeric) AS orderedquantity, resource.id as resource_id
   FROM materialflowresources_resource resource
	 JOIN deliveries_orderedproduct orderedproduct ON (orderedproduct.product_id = resource.product_id )
	 JOIN deliveries_delivery delivery ON (orderedproduct.delivery_id = delivery.id AND delivery.active = true AND delivery.location_id = resource.location_id AND (delivery.state::text = ANY (ARRAY['01draft'::character varying::text,
		'02prepared'::character varying::text, '03duringCorrection'::character varying::text, '05approved'::character varying::text])))
group by resource.id;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto_internal AS
 SELECT row_number() OVER () AS id,
    resource.location_id,
    resource.product_id,
    sum(resource.quantity) AS quantity,
    COALESCE(orderedquantity.orderedquantity, 0::numeric) as orderedquantity,
    ( SELECT sum(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum
           FROM warehouseminimalstate_warehouseminimumstate
          WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id) AS minimumstate
   FROM materialflowresources_resource resource
   left join materialflowresources_orderedquantity orderedquantity ON (orderedquantity.resource_id = resource.id)
  GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity;


CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto AS
select internal.*, location.number as locationNumber, location.name as locationName, product.number as productNumber, product.name as productName, product.unit as productUnit
	from materialflowresources_warehousestocklistdto_internal internal
	join materialflow_location location ON (location.id = internal.location_id)
	join basic_product product ON (product.id = internal.product_id);

-- end

-- versionable
CREATE OR REPLACE FUNCTION update_version() RETURNS VOID AS $$ DECLARE row record;  BEGIN FOR row IN SELECT tablename FROM pg_tables p INNER  JOIN information_schema.columns c on p.tablename = c.table_name WHERE c.table_schema = 'public' and p.schemaname = 'public' and c.column_name = 'id' and data_type = 'bigint'  LOOP  EXECUTE 'ALTER TABLE ' || quote_ident(row.tablename) || ' ADD entityVersion BIGINT DEFAULT 0;';  END LOOP;  END;  $$ LANGUAGE 'plpgsql';
SELECT * FROM update_version();
DROP FUNCTION update_version();
--end