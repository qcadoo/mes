-- views
-- by kasi 01.09.2016
CREATE SEQUENCE deliveries_orderedproductdto_id_seq;

DROP VIEW IF EXISTS deliveries_orderedproductdto;
CREATE OR REPLACE VIEW deliveries_orderedproductdto AS
	SELECT
		orderedproduct.id as id,
		orderedproduct.succession as succession,
		orderedproduct.orderedquantity as orderedquantity,
		orderedproduct.priceperunit as priceperunit,
		orderedproduct.totalprice as totalprice,
		orderedproduct.conversion as conversion,
		orderedproduct.additionalquantity as additionalquantity,
		orderedproduct.description as description,
		orderedproduct.actualversion as actualVersion,
		del.id as delivery,
		del.id as deliveryId,
		del.supplier_id as supplier,
		product.number as productNumber,
		product.name as productName,
		product.norm as productNorm,
		product.unit as productUnit,
		addcode.code as additionalCode,
		offer.number as offerNumber,
		operation.number as operationNumber,
		(SELECT catalognumber FROM productcatalognumbers_productcatalognumbers WHERE product_id = product.id and company_id=del.supplier_id) as productCatalogNumber,
		CASE
			WHEN addcode.id IS NULL THEN (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = del.id and product_id = product.id and additionalcode_id IS NULL GROUP BY product_id, additionalcode_id)
			ELSE (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = del.id and product_id = product.id and additionalcode_id = addcode.id GROUP BY product_id, additionalcode_id)
		END AS deliveredQuantity,
		CASE
			WHEN addcode.id IS NULL THEN orderedproduct.orderedquantity - (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = del.id and product_id = product.id and additionalcode_id IS NULL GROUP BY product_id, additionalcode_id)
			ELSE orderedproduct.orderedquantity - (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = del.id and product_id = product.id and additionalcode_id = addcode.id GROUP BY product_id, additionalcode_id)
		END AS leftToReceiveQuantity

	FROM deliveries_orderedproduct orderedproduct
		LEFT JOIN deliveries_delivery del ON orderedproduct.delivery_id = del.id
		LEFT JOIN basic_product product ON orderedproduct.product_id = product.id
		LEFT JOIN supplynegotiations_offer offer ON orderedproduct.offer_id = offer.id
		LEFT JOIN technologies_operation operation ON orderedproduct.operation_id = operation.id
		LEFT JOIN basic_additionalcode addcode ON orderedproduct.additionalcode_id = addcode.id;

CREATE SEQUENCE deliveries_deliveredproductdto_id_seq;

DROP VIEW IF EXISTS deliveries_deliveredproductdto;
CREATE OR REPLACE VIEW deliveries_deliveredproductdto AS
	SELECT
		deliveredproduct.id as id,
		deliveredproduct.succession as succession,
		deliveredproduct.damagedquantity as damagedquantity,
		deliveredproduct.deliveredquantity as deliveredquantity,
		deliveredproduct.priceperunit as priceperunit,
		deliveredproduct.totalprice as totalprice,
		deliveredproduct.conversion as conversion,
		deliveredproduct.additionalquantity as additionalquantity,
		deliveredproduct.iswaste as iswaste,
		del.id as delivery,
		del.id as deliveryId,
		del.supplier_id as supplier,
		product.number as productNumber,
		product.name as productName,
		product.unit as productUnit,
		addcode.code as additionalCode,
		offer.number as offerNumber,
		operation.number as operationNumber,
		slocation.number as storageLocationNumber,
		pnumber.number as palletNumber,
		(SELECT catalognumber FROM productcatalognumbers_productcatalognumbers WHERE product_id = product.id and company_id=del.supplier_id) as productCatalogNumber
	FROM deliveries_deliveredproduct deliveredproduct
		LEFT JOIN deliveries_delivery del ON deliveredproduct.delivery_id = del.id
		LEFT JOIN basic_product product ON deliveredproduct.product_id = product.id
		LEFT JOIN supplynegotiations_offer offer ON deliveredproduct.offer_id = offer.id
		LEFT JOIN technologies_operation operation ON deliveredproduct.operation_id = operation.id
		LEFT JOIN basic_additionalcode addcode ON deliveredproduct.additionalcode_id = addcode.id
		LEFT JOIN materialflowresources_storagelocation slocation ON deliveredproduct.storagelocation_id = slocation.id
		LEFT JOIN basic_palletnumber pnumber ON deliveredproduct.palletnumber_id = pnumber.id;

-- end

-- incomplete resource fields
-- last touched 18.08.2016 by pako

ALTER TABLE materialflowresources_position ADD COLUMN waste boolean DEFAULT false;
ALTER TABLE materialflowresources_resource ADD COLUMN waste boolean DEFAULT false;
INSERT INTO materialflowresources_documentpositionparametersitem(id, name, checked, editable, ordering, parameters_id) VALUES (19, 'waste', true, true, 19, 1);

--

-- assortments
-- last touched 07.09.2016 by kama

CREATE TABLE basic_assortment
(
  id bigint NOT NULL,
  name character varying(255),
  active boolean DEFAULT true,
  CONSTRAINT basic_assortment_pkey PRIMARY KEY (id)
);

CREATE TABLE basic_assortmentelement
(
  id bigint NOT NULL,
  descriptiontype character varying(255),
  description character varying(255),
  assortment_id bigint,
  CONSTRAINT basic_assortmentelement_pkey PRIMARY KEY (id),
  CONSTRAINT assortmentelement_assortment_fkey FOREIGN KEY (assortment_id)
      REFERENCES basic_assortment (id) DEFERRABLE
);

ALTER TABLE basic_product ADD COLUMN assortment_id bigint;
ALTER TABLE basic_product
  ADD CONSTRAINT product_assortment_fkey FOREIGN KEY (assortment_id)
      REFERENCES basic_assortment (id) DEFERRABLE;

INSERT INTO qcadoomodel_dictionary(
            name, pluginidentifier, active)
    SELECT 'descriptionTypes', 'basic', TRUE
    WHERE NOT EXISTS (
        SELECT id FROM qcadoomodel_dictionary WHERE name = 'descriptionTypes');

-- end

-- last touched 09.09.2016 by kasi

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
    where storageLocation.active = true
   	group by locationNumber, storageLocationNumber, productNumber, productName, additionalCode, productUnit, productAdditionalUnit;


CREATE OR REPLACE VIEW materialflowresources_storagelocationdto AS
	select row_number() OVER () AS id, internal.*
	from materialflowresources_storagelocationdto_internal internal;

-- end