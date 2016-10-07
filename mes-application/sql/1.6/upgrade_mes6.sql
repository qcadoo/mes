
-- added description to documentDto
-- last touched 21.09.2016 by kama
DROP VIEW materialflowresources_documentdto;

CREATE OR REPLACE VIEW materialflowresources_documentdto AS
	SELECT
		document.id AS id,
		document.number AS number,
		document.name AS name,
		document.type AS type,
		document.time AS time,
		document.state AS state,
		document.active AS active,
		locationfrom.id::integer AS locationfrom_id,
		locationfrom.name AS locationfromname,
		locationto.id::integer AS locationto_id,
		locationto.name AS locationtoname,
		company.id::integer AS company_id,
		company.name AS companyname,
		document.description AS description,
		securityuser.id::integer AS user_id,
		securityuser.firstname || ' ' || securityuser.lastname AS username,
		maintenanceevent.id::integer AS maintenanceevent_id,
		maintenanceevent.number AS maintenanceeventnumber,
		plannedevent.id::integer AS plannedevent_id,
		plannedevent.number AS plannedeventnumber,
		delivery.id::integer AS delivery_id,
		delivery.number AS deliverynumber,
		ordersorder.id::integer AS order_id,
		ordersorder.number AS ordernumber,
		suborder.id::integer AS suborder_id,
        suborder.number AS subordernumber
	FROM materialflowresources_document document
	LEFT JOIN materialflow_location locationfrom
		ON locationfrom.id = document.locationfrom_id
	LEFT JOIN materialflow_location locationto
		ON locationto.id = document.locationto_id
	LEFT JOIN basic_company company
		ON company.id = document.company_id
	LEFT JOIN qcadoosecurity_user securityuser
		ON securityuser.id = document.user_id
	LEFT JOIN cmmsmachineparts_maintenanceevent maintenanceevent
		ON maintenanceevent.id = document.maintenanceevent_id
	LEFT JOIN cmmsmachineparts_plannedevent plannedevent
		ON plannedevent.id = document.plannedevent_id
	LEFT JOIN deliveries_delivery delivery
		ON delivery.id = document.delivery_id
	LEFT JOIN orders_order ordersorder
		ON ordersorder.id = document.order_id
	LEFT JOIN subcontractorportal_suborder suborder
		ON suborder.id = document.suborder_id;

-- end


-- table: basic_address
-- last touched 04.10.2016 by lupo

ALTER TABLE basic_address ADD COLUMN externalnumber character varying(255);

-- end
