-- views
-- by kasi 01.09.2016
CREATE SEQUENCE deliveries_orderedproductdto_id_seq;

DROP VIEW IF EXISTS deliveries_orderedproductdto;
CREATE OR REPLACE VIEW deliveries_orderedproductdto AS
	SELECT
		orderedproduct.id AS id,
		orderedproduct.succession AS succession,
		orderedproduct.orderedquantity AS orderedquantity,
		orderedproduct.priceperunit AS priceperunit,
		orderedproduct.totalprice AS totalprice,
		orderedproduct.conversion AS conversion,
		orderedproduct.additionalquantity AS additionalquantity,
		orderedproduct.description AS description,
		orderedproduct.actualversion AS actualVersion,
		delivery.id AS delivery,
		delivery.id::integer AS deliveryId,
		delivery.supplier_id AS supplier,
		product.number AS productNumber,
		product.name AS productName,
		product.norm AS productNorm,
		product.unit AS productUnit,
		addcode.code AS additionalCode,
		offer.number AS offerNumber,
		operation.number AS operationNumber,
		(SELECT catalognumber FROM productcatalognumbers_productcatalognumbers WHERE product_id = product.id AND company_id = delivery.supplier_id) AS productCatalogNumber,
		CASE
			WHEN addcode.id IS NULL THEN (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = delivery.id AND product_id = product.id AND additionalcode_id IS NULL GROUP BY product_id, additionalcode_id)
			ELSE (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = delivery.id AND product_id = product.id AND additionalcode_id = addcode.id GROUP BY product_id, additionalcode_id)
		END AS deliveredQuantity,
		CASE
			WHEN addcode.id IS NULL THEN orderedproduct.orderedquantity - (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = delivery.id AND product_id = product.id AND additionalcode_id IS NULL GROUP BY product_id, additionalcode_id)
			ELSE orderedproduct.orderedquantity - (SELECT SUM (deliveredquantity) FROM deliveries_deliveredproduct WHERE delivery_id = delivery.id AND product_id = product.id AND additionalcode_id = addcode.id GROUP BY product_id, additionalcode_id)
		END AS leftToReceiveQuantity

	FROM deliveries_orderedproduct orderedproduct
		LEFT JOIN deliveries_delivery delivery ON orderedproduct.delivery_id = delivery.id
		LEFT JOIN basic_product product ON orderedproduct.product_id = product.id
		LEFT JOIN supplynegotiations_offer offer ON orderedproduct.offer_id = offer.id
		LEFT JOIN technologies_operation operation ON orderedproduct.operation_id = operation.id
		LEFT JOIN basic_additionalcode addcode ON orderedproduct.additionalcode_id = addcode.id;

CREATE SEQUENCE deliveries_deliveredproductdto_id_seq;

DROP VIEW IF EXISTS deliveries_deliveredproductdto;
CREATE OR REPLACE VIEW deliveries_deliveredproductdto AS
	SELECT
		deliveredproduct.id AS id,
		deliveredproduct.succession AS succession,
		deliveredproduct.damagedquantity AS damagedquantity,
		deliveredproduct.deliveredquantity AS deliveredquantity,
		deliveredproduct.priceperunit AS priceperunit,
		deliveredproduct.totalprice AS totalprice,
		deliveredproduct.conversion AS conversion,
		deliveredproduct.additionalquantity AS additionalquantity,
		deliveredproduct.iswaste AS iswaste,
		delivery.id AS delivery,
		delivery.id::integer AS deliveryId,
		delivery.supplier_id AS supplier,
		product.number AS productNumber,
		product.name AS productName,
		product.unit AS productUnit,
		addcode.code AS additionalCode,
		offer.number AS offerNumber,
		operation.number AS operationNumber,
		slocation.number AS storageLocationNumber,
		pnumber.number AS palletNumber,
		(SELECT catalognumber FROM productcatalognumbers_productcatalognumbers WHERE product_id = product.id AND company_id = delivery.supplier_id) AS productCatalogNumber
	FROM deliveries_deliveredproduct deliveredproduct
		LEFT JOIN deliveries_delivery delivery ON deliveredproduct.delivery_id = delivery.id
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


-- table: basic_company
-- last touched 02.09.2016 by lupo

ALTER TABLE basic_company ADD COLUMN issupplier boolean;
ALTER TABLE basic_company ALTER COLUMN issupplier SET DEFAULT false;

ALTER TABLE basic_company ADD COLUMN isreceiver boolean;
ALTER TABLE basic_company ALTER COLUMN isreceiver SET DEFAULT false;

UPDATE basic_company SET issupplier = false, isreceiver = false;

-- end


-- table: basic_address
-- last touched 02.09.2016 by lupo

CREATE TABLE basic_address
(
  id bigint NOT NULL,
  company_id bigint,
  addresstype character varying(255),
  "number" character varying(255),
  name character varying(255),
  phone character varying(255),
  email character varying(255),
  website character varying(255),
  street character varying(255),
  house character varying(30),
  flat character varying(30),
  zipcode character varying(255),
  city character varying(255),
  state character varying(255),
  country_id bigint,
  contactperson character varying(255),
  canbedeleted boolean DEFAULT false,
  active boolean DEFAULT true,
  CONSTRAINT basic_address_pkey PRIMARY KEY (id),
  CONSTRAINT address_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT address_country_fkey FOREIGN KEY (country_id)
		REFERENCES basic_country (id) DEFERRABLE
);

-- end


-- table: materialflowresources_document
-- last touched 02.09.2016 by lupo

ALTER TABLE materialflowresources_document ADD COLUMN address_id bigint;

-- end
