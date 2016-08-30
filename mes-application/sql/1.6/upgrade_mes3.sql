-- added corrections to production tracking
-- last touched 12.07 by pako
ALTER TABLE productioncounting_productiontracking ADD COLUMN correction_id bigint;
ALTER TABLE productioncounting_productiontracking
  ADD CONSTRAINT productiontracking_productiontracking_c FOREIGN KEY (correction_id)
      REFERENCES productioncounting_productiontracking (id) DEFERRABLE;

CREATE OR REPLACE VIEW productioncounting_productiontrackingdto AS
	SELECT
		productiontracking.id AS id,
		productiontracking.number AS number,
		productiontracking.state AS state,
		productiontracking.createdate AS createdate,
		productiontracking.lasttracking AS lasttracking,
		productiontracking.timerangefrom AS timerangefrom,
		productiontracking.timerangeto AS timerangeto,
		productiontracking.active AS active,
		ordersorder.id::integer AS order_id,
		ordersorder.number AS ordernumber,
		ordersorder.state AS orderstate,
		technologyoperationcomponent.id::integer AS technologyoperationcomponent_id,
		(CASE WHEN technologyoperationcomponent IS NULL THEN '' ELSE (technologyoperationcomponent.nodenumber::text || ' '::text) || operation.name::text END) AS technologyoperationcomponentnumber,
		operation.id::integer AS operation_id,
		shift.id::integer AS shift_id,
		shift.name AS shiftname,
		staff.id::integer AS staff_id,
		staff.name || ' ' || staff.surname AS staffname,
		division.id::integer AS division_id,
		division.number AS divisionnumber,
		subcontractor.id::integer AS subcontractor_id,
		subcontractor.name AS subcontractorname,
        repairorderdto.id::integer AS repairorder_id,
        repairorderdto.number AS repairordernumber,
		productiontrackingcorrection.number AS correctionNumber
	FROM productioncounting_productiontracking productiontracking
	LEFT JOIN orders_order ordersorder
		ON ordersorder.id = productiontracking.order_id
	LEFT JOIN technologies_technologyoperationcomponent technologyoperationcomponent
		ON technologyoperationcomponent.id = productiontracking.technologyoperationcomponent_id
	LEFT JOIN technologies_operation operation
		ON operation.id = technologyoperationcomponent.operation_id
	LEFT JOIN basic_shift shift
		ON shift.id = productiontracking.shift_id
	LEFT JOIN basic_staff staff
		ON staff.id = productiontracking.staff_id
	LEFT JOIN basic_division division
		ON division.id = productiontracking.division_id
	LEFT JOIN basic_company subcontractor ON subcontractor.id = productiontracking.subcontractor_id
	LEFT JOIN productioncounting_productiontracking productiontrackingcorrection ON productiontrackingcorrection.id = productiontracking.correction_id
	LEFT JOIN repairs_repairorderdto repairorderdto
        ON repairorderdto.id = productiontracking.repairorder_id;

ALTER TABLE productioncounting_productiontracking ADD COLUMN iscorrection boolean DEFAULT false;

-- end

-- removed corrected entries from production tracking grouped by product
-- last touched 27.07 by pako

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductincomponentdto AS
SELECT
		trackingoperationproductincomponent.id AS id,
		productiontracking.id::integer AS productiontracking_id,
		product.id::integer AS product_id,
		product.number AS productnumber,
		product.unit AS productunit,
		CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (
			SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum
		) ELSE (
			SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum
		) END AS plannedquantity,
		trackingoperationproductincomponent.usedquantity AS usedquantity,
        batch.number AS batchnumber
	FROM productioncounting_trackingoperationproductincomponent trackingoperationproductincomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductincomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductincomponent.product_id
	LEFT JOIN advancedgenealogy_batch batch ON batch.id = trackingoperationproductincomponent.batch_id
	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1
		ON (
			productioncountingquantity_1.order_id = productiontracking.order_id
			AND productioncountingquantity_1.product_id = trackingoperationproductincomponent.product_id
   			AND productioncountingquantity_1.role::text = '01used'::text
   		)
   	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2
   		ON (
   			productioncountingquantity_2.order_id = productiontracking.order_id
   			AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id
			AND productioncountingquantity_2.product_id = trackingoperationproductincomponent.product_id
			AND productioncountingquantity_2.role::text = '01used'::text
		)
	WHERE productiontracking.state NOT IN ('03declined'::text,'04corrected'::text)
	GROUP BY
		trackingoperationproductincomponent.id,
		productiontracking.id,
		product.id,
		product.number,
		product.unit,
		trackingoperationproductincomponent.usedquantity,
		productiontracking.technologyoperationcomponent_id,
		batch.number;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductoutcomponentdto AS
	SELECT
		trackingoperationproductoutcomponent.id AS id,
		productiontracking.id::integer AS productiontracking_id,
		product.id::integer AS product_id,
		product.number AS productnumber,
		product.unit AS productunit,
		CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (
			SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum
		) ELSE (
			SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum
		) END AS plannedquantity,
		trackingoperationproductoutcomponent.usedquantity AS usedquantity,
        batch.number AS batchnumber
	FROM productioncounting_trackingoperationproductoutcomponent trackingoperationproductoutcomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductoutcomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductoutcomponent.product_id
	LEFT JOIN advancedgenealogy_batch batch ON batch.id = trackingoperationproductoutcomponent.batch_id
	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1
		ON (
			productioncountingquantity_1.order_id = productiontracking.order_id
			AND productioncountingquantity_1.product_id = trackingoperationproductoutcomponent.product_id
   			AND productioncountingquantity_1.role::text = '02produced'::text
   		)
   	LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2
   		ON (
   			productioncountingquantity_2.order_id = productiontracking.order_id
   			AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id
			AND productioncountingquantity_2.product_id = trackingoperationproductoutcomponent.product_id
			AND productioncountingquantity_2.role::text = '02produced'::text
		)
	WHERE productiontracking.state NOT IN ('03declined'::text,'04corrected'::text)
	GROUP BY
		trackingoperationproductoutcomponent.id,
		productiontracking.id,
		product.id,
		product.number,
		product.unit,
		trackingoperationproductoutcomponent.usedquantity,
		productiontracking.technologyoperationcomponent_id,
		batch.number;
-- end
		
-- new parameter
-- last touched 4.08 by pako

ALTER TABLE basic_parameter ADD COLUMN trackingcorrectionrecalculatepps boolean DEFAULT false;

-- end

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
ALTER TABLE masterorders_masterorder ADD COLUMN dateofreceipt timestamp without time zone;

ALTER TABLE masterorders_masterorderproduct ADD COLUMN lefttorelease numeric(14,5);
ALTER TABLE masterorders_masterorderproduct ADD COLUMN comments text;
ALTER TABLE masterorders_masterorderproduct ADD COLUMN masterorderpositionstatus character varying(255);


-- materialflowresources_document
-- last touched 18.08.2016 by kasi
ALTER TABLE materialflowresources_document ADD COLUMN createlinkedpzdocument boolean;
ALTER TABLE materialflowresources_document ADD COLUMN linkedpzdocumentlocation_id bigint;
ALTER TABLE materialflowresources_document
  ADD CONSTRAINT document_linkedpzdocumentlocation_fkey FOREIGN KEY (linkedpzdocumentlocation_id)
      REFERENCES materialflow_location (id) DEFERRABLE;
-- end

-- made storage locations activable
-- last touched 23.08.2016 by pako

ALTER TABLE materialflowresources_storagelocation ADD COLUMN active boolean DEFAULT true;

-- end

-- added parameter
-- last touched 29.08.2016 by kama

ALTER TABLE basic_parameter ADD COLUMN deliveredbiggerthanordered boolean DEFAULT true;

-- end