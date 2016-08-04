-- added corrections to production tracking
-- last touched 12.07 by pako

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
	LEFT JOIN productioncounting_productiontracking productiontrackingcorrection ON productiontrackingcorrection.id = productiontracking.correction_id; 

ALTER TABLE productioncounting_productiontracking ADD COLUMN correction_id bigint;
ALTER TABLE productioncounting_productiontracking
  ADD CONSTRAINT productiontracking_productiontracking_c FOREIGN KEY (correction_id)
      REFERENCES productioncounting_productiontracking (id) DEFERRABLE;

ALTER TABLE productioncounting_productiontracking ADD COLUMN iscorrection boolean;
ALTER TABLE productioncounting_productiontracking ALTER COLUMN iscorrection SET DEFAULT false;
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

ALTER TABLE basic_parameter ADD COLUMN trackingcorrectionrecalculatepps boolean;

-- end