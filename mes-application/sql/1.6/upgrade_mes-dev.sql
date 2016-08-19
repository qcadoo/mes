-- storageLocationDto
-- last touched 12.08.2016 by kama

CREATE SEQUENCE materialFlowResources_storagelocationdto_id_seq;

CREATE OR REPLACE VIEW materialflowresources_storagelocationdto_internal AS
    select location.number as locationNumber, storageLocation.number as storageLocationNumber,
    	COALESCE(product.number, storageProduct.number) as productNumber, COALESCE(product.name, storageProduct.name) as productName, resourceCode.code as additionalCode,
    	COALESCE(SUM(resource.quantity), 0::numeric) as resourceQuantity, COALESCE(product.unit, storageProduct.unit) as productUnit,
    	COALESCE(SUM(resource.quantityinadditionalunit), 0::numeric) as quantityInAdditionalUnit,
    	COALESCE(product.additionalunit, product.unit, storageProduct.additionalunit, storageProduct.unit) as productAdditionalUnit
    from materialflowresources_storagelocation storageLocation
   	join materialflow_location location on storageLocation.location_id = location.id
   	left join materialflowresources_resource resource on resource.storagelocation_id = storageLocation.id
   	left join basic_product product on product.id = resource.product_id
   	left join basic_product storageProduct on storageProduct.id = storageLocation.product_id
   	left join basic_additionalcode resourceCode on resourceCode.id = resource.additionalcode_id
   	group by locationNumber, storageLocationNumber, productNumber, productName, additionalCode, productUnit, productAdditionalUnit;


CREATE OR REPLACE VIEW materialflowresources_storagelocationdto AS
	select row_number() OVER () AS id, internal.*
	from materialflowresources_storagelocationdto_internal internal;
-- end

-- masterOrder
-- last touched 19.08.2016 by kasi
ALTER TABLE masterorders_masterorder ADD COLUMN lefttorelease numeric(14,5);
ALTER TABLE masterorders_masterorder ADD COLUMN comments text;
ALTER TABLE masterorders_masterorder ADD COLUMN masterorderpositionstatus character varying(255);

ALTER TABLE masterorders_masterorderproduct ADD COLUMN lefttorelease numeric(14,5);
ALTER TABLE masterorders_masterorderproduct ADD COLUMN comments text;
ALTER TABLE masterorders_masterorderproduct ADD COLUMN masterorderpositionstatus character varying(255);

-- end