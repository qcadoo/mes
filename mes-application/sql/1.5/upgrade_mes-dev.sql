
-- worker to change in order
-- last touched 27.01.2016 by kama

ALTER TABLE orders_order ADD COLUMN workertochange character varying(255);

-- end


-- productioncounting_productiontrackingdto
-- last touched 09.02.2016 by lupo

CREATE SEQUENCE productioncounting_productiontrackingdto_id_seq;

CREATE OR REPLACE VIEW productioncounting_productiontrackingdto AS
	SELECT
		productiontracking.id::integer AS id,
		productiontracking.number AS productiontrackingnumber,
		productiontracking.state AS productiontrackingstate,
		productiontracking.createdate AS productiontrackingcreatedate,
		productiontracking.lasttracking AS productiontrackinglasttracking,
		productiontracking.timerangefrom AS productiontrackingtimerangefrom,
		productiontracking.timerangeto AS productiontrackingtimerangeto,
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
		staff.number || ' - ' || staff.name || ' ' || staff.surname AS staffname,
		division.id::integer AS division_id,
		division.number AS divisionnumber,
		subcontractor.id AS subcontractor_id,
		subcontractor.name AS subcontractorname
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
	LEFT JOIN basic_company subcontractor ON subcontractor.id = productiontracking.subcontractor_id;

-- end
