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
-- last touched 19.02.2016 by wesi

drop view cmmsmachineparts_plannedEventListDto;

ALTER TABLE cmmsmachineparts_plannedevent ALTER COLUMN description TYPE character varying(600);

create or replace view cmmsmachineparts_plannedEventListDto as select
    e.id, e.number, e.type,
    owner.name || ' ' || owner.surname  as ownerName,
    e.description, factory.number as factoryNumber,
    factory.id ::integer as factory_id,
    division.number as divisionNumber,
    division.id::integer as division_id,
    productionLine.number as productionLineNumber,
    workstation.number as workstationNumber,
    subassembly.number as subassemblyNumber,
    e.date::timestamp without time zone AS date,
    e.counter,
    e.createUser,
    e.createDate,
    e.state,
    context.id as plannedEventContext_id,
    workstation.id AS workstation_id,
    subassembly.id AS subassembly_id,
    company.id AS company_id,
    sourcecost.id AS sourcecost_id
    from cmmsmachineparts_plannedevent e
    left join basic_staff owner on (e.owner_id = owner.id)
    join basic_factory factory on (e.factory_id = factory.id)
    join basic_division division on (e.division_id = division.id)
    left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id)
    left join basic_workstation workstation on (e.workstation_id = workstation.id)
    left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id)
    left join cmmsmachineparts_plannedeventcontext context on (e.plannedeventcontext_id = context.id)
    left join basic_company company ON e.company_id = company.id
    left join cmmsmachineparts_sourcecost sourcecost ON e.sourcecost_id = sourcecost.id;


  drop view cmmsmachineparts_maintenanceEventListDto;

  create or replace view cmmsmachineparts_maintenanceEventListDto as
  select
      e.id,
      e.number,
      e.type,
      staff.name || ' ' || staff.surname  as personReceivingName,
      e.description, faultType.name as faultTypeNumber,
      factory.number as factoryNumber,
      division.number as divisionNumber,
      factory.id::integer as factory_id,
      division.id::integer as division_id,
      productionLine.number as productionLineNumber,
      workstation.number as workstationNumber,
      subassembly.number as subassemblyNumber,
      e.createUser,
      e.createDate,
      e.state,
      context.id as maintenanceEventContext_id
      from cmmsmachineparts_maintenanceevent e
      left join basic_staff staff on (e.personreceiving_id = staff.id)
      join cmmsmachineparts_faulttype faultType on (e.faulttype_id = faultType.id)
      join basic_factory factory on (e.factory_id = factory.id)
      join basic_division division on (e.division_id = division.id)
      left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id)
      left join basic_workstation workstation on (e.workstation_id = workstation.id)
      left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id)
      left join cmmsmachineparts_maintenanceeventcontext context on (e.maintenanceeventcontext_id = context.id);
-- end

-- start

drop view productioncounting_productiontrackingforproductgroupeddto;
drop view productioncounting_productiontrackingforproductdto;
drop view productioncounting_trackingoperationproductcomponentdto;
drop view productioncounting_trackingoperationproductoutcomponentdto;
drop view productioncounting_trackingoperationproductincomponentdto;
drop view productioncounting_productiontrackingdto;

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
		product.name,
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