-- assignment to shift changes
-- last touched 11.02.2016 by pako

CREATE TABLE assignmenttoshift_multiassignmenttoshift
(
  id bigint NOT NULL,
  productionline_id bigint,
  occupationtype character varying(255),
  occupationtypename character varying(255),
  occupationtypeenum character varying(255),
  masterorder_id bigint,
  assignmenttoshift_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT assignmenttoshift_multiassignmenttoshift_pkey PRIMARY KEY (id),
  CONSTRAINT multiassignmenttoshift_masterorder_fkey FOREIGN KEY (masterorder_id)
      REFERENCES masterorders_masterorder (id) DEFERRABLE,
  CONSTRAINT multiassignmenttoshift_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT multiassignmenttoshift_assignmenttoshift_fkey FOREIGN KEY (assignmenttoshift_id)
      REFERENCES assignmenttoshift_assignmenttoshift (id) DEFERRABLE
);

CREATE TABLE jointable_multiassignmenttoshift_staff
(
  multiassignmenttoshift_id bigint NOT NULL,
  staff_id bigint NOT NULL,
  CONSTRAINT jointable_multiassignmenttoshift_staff_pkey PRIMARY KEY (multiassignmenttoshift_id, staff_id),
  CONSTRAINT staff_multiassignmenttoshift_fkey FOREIGN KEY (staff_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT assignmenttoshift_multiassignmenttoshift_staff_fkey FOREIGN KEY (multiassignmenttoshift_id)
      REFERENCES assignmenttoshift_multiassignmenttoshift (id) DEFERRABLE
);

ALTER TABLE assignmenttoshift_staffassignmenttoshift ADD COLUMN description character varying(255);

-- end

-- cmmsmachineparts_plannedevent
-- last touched 24.08.2016 by lupo

DROP VIEW cmmsmachineparts_plannedeventlistdto;

ALTER TABLE cmmsmachineparts_plannedevent ALTER COLUMN description TYPE character varying(600);

CREATE OR REPLACE VIEW cmmsmachineparts_plannedeventlistdto AS
    SELECT
        plannedevent.id AS id,
        plannedevent.number AS number,
        plannedevent.type AS type,
        plannedevent.description AS description,
        plannedevent.date::TIMESTAMP WITHOUT time ZONE AS date,
        plannedevent.counter AS counter,
        plannedevent.createUser AS createuser,
        plannedevent.createDate AS createdate,
        plannedevent.state AS state,
        context.id AS plannedeventcontext_id,
        sourcecost.id AS sourcecost_id,
        staff.name || ' ' || staff.surname AS ownername,
        factory.id::integer AS factory_id,
        factory.number AS factorynumber,
        division.id::integer AS division_id,
        division.number AS divisionnumber,
        workstation.id::integer AS workstation_id,
        workstation.number AS workstationnumber,
        subassembly.id::integer AS subassembly_id,
        subassembly.number AS subassemblynumber,
        company.id::integer AS company_id,
        productionline.number AS productionlinenumber
    FROM cmmsmachineparts_plannedevent plannedevent
    LEFT JOIN cmmsmachineparts_plannedeventcontext context
        ON plannedevent.plannedeventcontext_id = context.id
    LEFT JOIN cmmsmachineparts_sourcecost sourcecost
        ON plannedevent.sourcecost_id = sourcecost.id
    LEFT JOIN basic_staff staff
        ON plannedevent.owner_id = staff.id
    LEFT JOIN basic_factory factory
        ON plannedevent.factory_id = factory.id
    LEFT JOIN basic_division division
        ON plannedevent.division_id = division.id
    LEFT JOIN basic_workstation workstation
        ON plannedevent.workstation_id = workstation.id
    LEFT JOIN basic_subassembly subassembly
        ON plannedevent.subassembly_id = subassembly.id
    LEFT JOIN basic_company company
        ON plannedevent.company_id = company.id
    LEFT JOIN productionlines_productionline productionline
        ON plannedevent.productionline_id = productionline.id;

  DROP VIEW cmmsmachineparts_maintenanceeventlistdto;

  CREATE OR REPLACE VIEW cmmsmachineparts_maintenanceeventlistdto AS
      SELECT
          maintenanceevent.id AS id,
          maintenanceevent.number AS number,
          maintenanceevent.type AS type,
          maintenanceevent.createuser AS createuser,
          maintenanceevent.createdate AS createdate,
          maintenanceevent.state AS state,
          maintenanceevent.description AS description,
          context.id AS maintenanceeventcontext_id,
          staff.name || ' ' || staff.surname AS personreceivingname,
          factory.id::integer AS factory_id,
          factory.number AS factorynumber,
          division.id::integer AS division_id,
          division.number AS divisionnumber, 
          workstation.number AS workstationnumber, 
          subassembly.number AS subassemblynumber, 
          faulttype.name AS faulttypename, 
          productionline.number AS productionlinenumber 
      FROM cmmsmachineparts_maintenanceevent maintenanceevent 
      LEFT JOIN cmmsmachineparts_maintenanceeventcontext context 
          ON maintenanceevent.maintenanceeventcontext_id = context.id 
      LEFT JOIN basic_staff staff 
          ON maintenanceevent.personreceiving_id = staff.id 
      LEFT JOIN basic_factory factory 
          ON maintenanceevent.factory_id = factory.id 
      LEFT JOIN basic_division division 
          ON maintenanceevent.division_id = division.id 
      LEFT JOIN basic_workstation workstation 
          ON maintenanceevent.workstation_id = workstation.id 
      LEFT JOIN basic_subassembly subassembly 
          ON maintenanceevent.subassembly_id = subassembly.id 
      LEFT JOIN cmmsmachineparts_faulttype faulttype 
          ON maintenanceevent.faulttype_id = faulttype.id 
      LEFT JOIN productionlines_productionline productionline 
          ON maintenanceevent.productionline_id = productionline.id;

-- end

-- start

DROP VIEW productioncounting_productiontrackingforproductgroupeddto;
DROP VIEW productioncounting_productiontrackingforproductdto;
DROP VIEW productioncounting_trackingoperationproductcomponentdto;
DROP VIEW productioncounting_trackingoperationproductoutcomponentdto;
DROP VIEW productioncounting_trackingoperationproductincomponentdto;
DROP VIEW productioncounting_productiontrackingdto;

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
		trackingoperationproductincomponent.usedquantity AS usedquantity
	FROM productioncounting_trackingoperationproductincomponent trackingoperationproductincomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductincomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductincomponent.product_id
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
	GROUP BY
		trackingoperationproductincomponent.id,
		productiontracking.id,
		product.id,
		product.number,
		product.unit,
		trackingoperationproductincomponent.usedquantity,
		productiontracking.technologyoperationcomponent_id;


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
		trackingoperationproductoutcomponent.usedquantity AS usedquantity
	FROM productioncounting_trackingoperationproductoutcomponent trackingoperationproductoutcomponent
	LEFT JOIN productioncounting_productiontracking productiontracking
		ON productiontracking.id = trackingoperationproductoutcomponent.productiontracking_id
	LEFT JOIN basic_product product
		ON product.id = trackingoperationproductoutcomponent.product_id
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
	GROUP BY
		trackingoperationproductoutcomponent.id,
		productiontracking.id,
		product.id,
		product.number,
		product.unit,
		trackingoperationproductoutcomponent.usedquantity,
		productiontracking.technologyoperationcomponent_id;


CREATE OR REPLACE VIEW productioncounting_trackingoperationproductcomponentdto AS
	SELECT
		row_number() OVER () AS id,
		trackingoperationproductcomponentdto.productiontracking_id::integer AS productiontracking_id,
    	trackingoperationproductcomponentdto.product_id::integer AS product_id,
    	trackingoperationproductcomponentdto.productnumber AS productnumber,
    	trackingoperationproductcomponentdto.productunit AS productunit,
    	trackingoperationproductcomponentdto.plannedquantity AS plannedquantity,
    	trackingoperationproductcomponentdto.usedquantity AS usedquantity
    FROM (
    	SELECT
    		trackingoperationproductincomponentdto.productiontracking_id,
			trackingoperationproductincomponentdto.product_id,
			trackingoperationproductincomponentdto.productnumber,
			trackingoperationproductincomponentdto.productunit,
			trackingoperationproductincomponentdto.plannedquantity,
			trackingoperationproductincomponentdto.usedquantity
        FROM productioncounting_trackingoperationproductincomponentdto trackingoperationproductincomponentdto
        UNION
		SELECT
			trackingoperationproductoutcomponentdto.productiontracking_id,
            trackingoperationproductoutcomponentdto.product_id,
            trackingoperationproductoutcomponentdto.productnumber,
            trackingoperationproductoutcomponentdto.productunit,
            trackingoperationproductoutcomponentdto.plannedquantity,
            trackingoperationproductoutcomponentdto.usedquantity
        FROM productioncounting_trackingoperationproductoutcomponentdto trackingoperationproductoutcomponentdto
    ) trackingoperationproductcomponentdto;


CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductdto AS
	SELECT
		trackingoperationproductcomponentdto.id AS id,
		productiontrackingdto.number AS number,
		productiontrackingdto.state AS state,
		productiontrackingdto.createdate AS createdate,
		productiontrackingdto.lasttracking AS lasttracking,
		productiontrackingdto.timerangefrom AS timerangefrom,
		productiontrackingdto.timerangeto AS timerangeto,
		productiontrackingdto.active AS active,
		productiontrackingdto.order_id::integer AS order_id,
		productiontrackingdto.ordernumber AS ordernumber,
		productiontrackingdto.orderstate AS orderstate,
		productiontrackingdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id,
		productiontrackingdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber,
		productiontrackingdto.operation_id::integer AS operation_id,
		productiontrackingdto.shift_id::integer AS shift_id,
		productiontrackingdto.shiftname AS shiftname,
		productiontrackingdto.staff_id::integer AS staff_id,
		productiontrackingdto.staffname AS staffname,
		productiontrackingdto.division_id::integer AS division_id,
		productiontrackingdto.divisionnumber AS divisionnumber,
		productiontrackingdto.subcontractor_id::integer AS subcontractor_id,
		productiontrackingdto.subcontractorname AS subcontractorname,
		trackingoperationproductcomponentdto.product_id::integer AS product_id,
		trackingoperationproductcomponentdto.productnumber AS productnumber,
		trackingoperationproductcomponentdto.productunit AS productunit,
		trackingoperationproductcomponentdto.plannedquantity AS plannedquantity,
		trackingoperationproductcomponentdto.usedquantity AS usedquantity,
		productiontrackingdto.id::integer AS productiontracking_id
	FROM productioncounting_trackingoperationproductcomponentdto trackingoperationproductcomponentdto
	LEFT JOIN productioncounting_productiontrackingdto productiontrackingdto
		ON productiontrackingdto.id = trackingoperationproductcomponentdto.productiontracking_id;


CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductgroupeddto AS
	SELECT
		row_number() OVER () AS id,
		productiontrackingforproductdto.active AS active,
		productiontrackingforproductdto.order_id::integer AS order_id,
    	productiontrackingforproductdto.ordernumber AS ordernumber,
    	productiontrackingforproductdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id,
    	productiontrackingforproductdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber,
    	productiontrackingforproductdto.operation_id AS operation_id,
    	productiontrackingforproductdto.product_id::integer AS product_id,
    	productiontrackingforproductdto.productnumber AS productnumber,
    	productiontrackingforproductdto.productunit AS productunit,
    	productiontrackingforproductdto.plannedquantity AS plannedquantity,
    	SUM(productiontrackingforproductdto.usedquantity) AS usedquantity
	FROM productioncounting_productiontrackingforproductdto productiontrackingforproductdto
	GROUP BY
		productiontrackingforproductdto.active,
		productiontrackingforproductdto.order_id,
		productiontrackingforproductdto.ordernumber,
		productiontrackingforproductdto.technologyoperationcomponent_id,
		productiontrackingforproductdto.technologyoperationcomponentnumber,
		productiontrackingforproductdto.operation_id,
		productiontrackingforproductdto.product_id,
		productiontrackingforproductdto.productnumber,
		productiontrackingforproductdto.productunit,
		productiontrackingforproductdto.plannedquantity;

-- end