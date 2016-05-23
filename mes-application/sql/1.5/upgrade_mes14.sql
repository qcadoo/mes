
ALTER TABLE materialflowresources_resource ADD COLUMN quantityinadditionalunit numeric(14,5);

ALTER TABLE materialflowresources_resource ADD COLUMN additionalcode_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN conversion numeric(12,5);
ALTER TABLE materialflowresources_resource ALTER COLUMN conversion SET DEFAULT 0::numeric;

ALTER TABLE materialflowresources_resource ADD COLUMN palletnumber_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN typeofpallet character varying(255);

ALTER TABLE materialflowresources_resource ADD COLUMN givenunit character varying(255);


-- ESILCO-16

CREATE TABLE materialflowresources_documentpositionparameters
(
  id bigint NOT NULL,
  CONSTRAINT materialflowresources_documentpositionparameters_pkey PRIMARY KEY (id)
);

CREATE TABLE materialflowresources_documentpositionparametersitem
(
  id bigint NOT NULL,
  checked boolean DEFAULT true,
  editable boolean DEFAULT true,
  parameters_id bigint,
  name character varying(255),
  ordering integer,
  CONSTRAINT materialflowresources_documentpositionparametersitem_pkey PRIMARY KEY (id)
);

ALTER TABLE materialflowresources_documentpositionparametersitem
  ADD CONSTRAINT documentpositionparametersitem_parameters_fkey FOREIGN KEY (parameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;


INSERT INTO materialflowresources_documentpositionparameters (id) VALUES
    (1);

INSERT INTO materialflowresources_documentpositionparametersitem (id,ordering,name, parameters_id, editable) VALUES
    (1,1,'act', 1, false),
    (2,2,'number', 1, false),
    (3,3,'product', 1, false),
    (4,4,'additionalCode', 1, true),
    (5,5,'quantity', 1, false),
    (6,6,'unit', 1, false),
    (7,7,'givenquantity', 1, false),
    (8,8,'givenunit', 1, false),
    (9,9,'conversion', 1, false),
    (10,10,'resource', 1, true),
    (11,11,'price', 1, true),
    (12,12,'batch', 1, true),
    (13,13,'productiondate', 1, true),
    (14,14,'expirationdate', 1, true),
    (15,15,'storageLocation', 1, true),
    (16,16,'palletNumber', 1, true),
    (17,17,'typeOfPallet', 1, true);

ALTER TABLE basic_parameter ADD COLUMN documentpositionparameters_id bigint;

ALTER TABLE basic_parameter
  ADD CONSTRAINT parammeter_documentpositionparameters_fkey FOREIGN KEY (documentpositionparameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;

-- end


ALTER TABLE materialflowresources_position ADD COLUMN additionalcode_id bigint;
ALTER TABLE materialflowresources_position ADD COLUMN conversion numeric(12,5) DEFAULT 0::numeric;
ALTER TABLE materialflowresources_position ADD COLUMN palletnumber_id bigint;
ALTER TABLE materialflowresources_position ADD COLUMN typeofpallet character varying(255);

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;


-- resource lookup changes
-- last touched 23.02.2016 by pako

ALTER TABLE materialflowresources_documentpositionparameters ADD COLUMN suggestresource boolean DEFAULT false;

-- end


-- added helper for master orders
-- last touched 22.04.2016 by kama

ALTER TABLE orders_order ADD COLUMN masterorderproductcomponent_id bigint;

ALTER TABLE orders_order
  ADD CONSTRAINT order_masterorderproduct_fkey FOREIGN KEY (masterorderproductcomponent_id)
      REFERENCES masterorders_masterorderproduct (id) DEFERRABLE;

-- end


-- last touched 18.04.2016 by kasi
-- Table: productionpershift_dailyprogress

ALTER TABLE productionpershift_dailyprogress ADD COLUMN efficiencytime integer;

-- end


-- last touched 02.05.2016 by kasi
-- delete simpleMaterialBalance view

DELETE FROM qcadooview_item WHERE name = 'simpleMaterialBalance';
DELETE FROM qcadooview_view WHERE name = 'simpleMaterialBalanceList';

-- end


-- last touched 11.05.2016 by bafl
-- add "Type" column

DROP VIEW basic_subassemblylistdto;

CREATE OR REPLACE VIEW basic_subassemblylistdto AS
    SELECT
        subassembly.id,
        subassembly.active,
        subassembly.number,
        subassembly.name,
        workstation.number AS workstationNumber,
        subassembly.type,
        workstationType.number AS workstationTypeNumber,
        date(subassembly.productionDate) AS productionDate,
        date(event.maxDate) AS lastRepairsDate
    FROM basic_subassembly subassembly
    LEFT JOIN basic_workstation workstation
        ON subassembly.workstation_id = workstation.id
    JOIN basic_workstationtype workstationType
        ON subassembly.workstationtype_id = workstationType.id
    LEFT JOIN (
        SELECT
            subassembly_id AS subassemblyId,
            max(date) AS maxDate
        FROM cmmsmachineparts_plannedevent plannedevent
        WHERE
            plannedevent.state = '05realized'
            AND plannedevent.basedon = '01date'
            AND plannedevent.type = '02repairs'
        GROUP BY subassemblyId
    ) event
        ON event.subassemblyId = subassembly.id;

-- end


-- last touched 16.05.2016 by lupo

ALTER TABLE basic_parameter ALTER COLUMN consumptionofrawmaterialsbasedonstandards SET DEFAULT false;

UPDATE basic_parameter SET consumptionofrawmaterialsbasedonstandards = false WHERE consumptionofrawmaterialsbasedonstandards IS null;

-- end


-- orders_dto view
-- last touched 17.05.2016 by kama

CREATE SEQUENCE orders_orderdto_id_seq;

CREATE OR REPLACE VIEW orders_orderdto AS SELECT id, active, number, name, state, typeofproductionrecording FROM orders_order;

-- end


-- last touched 17.05.2016 by lupo

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
	GROUP BY
		trackingoperationproductincomponent.id,
		productiontracking.id,
		product.id,
		product.number,
		product.unit,
		trackingoperationproductincomponent.usedquantity,
		productiontracking.technologyoperationcomponent_id,
		batch.number;

-- end


-- last touched 17.05.2016 by lupo

ALTER TABLE basic_parameter DROP COLUMN hidedetailsinworkplans;

ALTER TABLE technologies_operation DROP COLUMN hidedescriptioninworkplans;
ALTER TABLE technologies_operation DROP COLUMN hidedetailsinworkplans;
ALTER TABLE technologies_operation DROP COLUMN hidetechnologyandorderinworkplans;
ALTER TABLE technologies_operation DROP COLUMN dontprintinputproductsinworkplans;
ALTER TABLE technologies_operation DROP COLUMN dontprintoutputproductsinworkplans;

ALTER TABLE technologies_technologyoperationcomponent DROP COLUMN hidedescriptioninworkplans;
ALTER TABLE technologies_technologyoperationcomponent DROP COLUMN hidedetailsinworkplans;
ALTER TABLE technologies_technologyoperationcomponent DROP COLUMN hidetechnologyandorderinworkplans;
ALTER TABLE technologies_technologyoperationcomponent DROP COLUMN imageurlinworkplan;
ALTER TABLE technologies_technologyoperationcomponent DROP COLUMN dontprintinputproductsinworkplans;
ALTER TABLE technologies_technologyoperationcomponent DROP COLUMN dontprintoutputproductsinworkplans;

DROP TABLE workplans_operationinputcolumn;
DROP TABLE workplans_operationoutputcolumn;

DROP TABLE workplans_technologyoperationinputcolumn;
DROP TABLE workplans_technologyoperationoutputcolumn;

-- end


-- last touched 18.05.2016 by lupo

CREATE SEQUENCE materialflowresources_documentdto_id_seq;

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
