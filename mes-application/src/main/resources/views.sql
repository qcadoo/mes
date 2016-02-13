--
-- ***************************************************************************
-- Copyright (c) 2010 Qcadoo Limited
-- Project: Qcadoo MES
-- Version: 1.4
--
-- This file is part of Qcadoo.
--
-- Qcadoo is free software; you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published
-- by the Free Software Foundation; either version 3 of the License,
-- or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty
-- of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
-- See the GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program; if not, write to the Free Software
-- Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
-- ***************************************************************************
--

-- This script is invoked when application starts with hbm2ddlAuto=create.

--  Qcadoo-model & Hibernate will automatically generate regular db table due to existence of warehouseStock.xml model,
-- we need to first drop this table, before create table view.

DROP TABLE IF EXISTS materialflowresources_warehousestock;

CREATE OR REPLACE FUNCTION create_warehouse_stock_view() RETURNS VOID AS $$ BEGIN IF EXISTS (SELECT * FROM information_schema.columns WHERE table_name = 'basic_parameter' AND column_name = 'tenantid') THEN CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity), 0::numeric) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_delivery.active = true AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity, tenantid FROM materialflowresources_resource GROUP BY tenantid, location_id, product_id; ELSE CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity),0) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.active = true AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id; END IF; END; $$ LANGUAGE 'plpgsql';

SELECT create_warehouse_stock_view();

DROP FUNCTION create_warehouse_stock_view();


CREATE OR REPLACE FUNCTION update_sequence() RETURNS VOID AS $$ DECLARE row record; BEGIN FOR row IN SELECT tablename FROM pg_tables p INNER JOIN information_schema.columns c ON p.tablename = c.table_name WHERE c.table_schema = 'public' AND p.schemaname = 'public' AND c.column_name = 'id' AND data_type = 'bigint' LOOP IF EXISTS (SELECT 0 FROM pg_class WHERE relname = '' || quote_ident(row.tablename) || '_id_seq') THEN	EXECUTE 'ALTER TABLE ' || quote_ident(row.tablename) || ' ALTER COLUMN id SET DEFAULT nextval(''' || quote_ident(row.tablename) || '_id_seq'');'; EXECUTE 'SELECT setval(''' || quote_ident(row.tablename) || '_id_seq'', COALESCE((SELECT MAX(id)+1 FROM ' || quote_ident(row.tablename) || '), 1), false);'; END IF; END LOOP; END; $$ LANGUAGE 'plpgsql';

SELECT * FROM update_sequence();

DROP FUNCTION update_sequence();


-- optymalizacja QCADOOCLS-4315

DROP TABLE IF EXISTS materialflowresources_warehousestocklistdto;

CREATE OR REPLACE VIEW materialflowresources_orderedquantity AS SELECT COALESCE(SUM(orderedproduct.orderedquantity), 0::numeric) AS orderedquantity, resource.id AS resource_id FROM materialflowresources_resource resource JOIN deliveries_orderedproduct orderedproduct ON (orderedproduct.product_id = resource.product_id) JOIN deliveries_delivery delivery ON (orderedproduct.delivery_id = delivery.id AND delivery.active = true AND delivery.location_id = resource.location_id AND (delivery.state::text = ANY (ARRAY['01draft'::character varying::text, '02prepared'::character varying::text, '03duringCorrection'::character varying::text, '05approved'::character varying::text]))) GROUP BY resource.id;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto_internal AS SELECT row_number() OVER () AS id, resource.location_id, resource.product_id::integer, SUM(resource.quantity) AS quantity, COALESCE(orderedquantity.orderedquantity, 0::numeric) AS orderedquantity, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id) AS minimumstate FROM materialflowresources_resource resource LEFT JOIN materialflowresources_orderedquantity orderedquantity ON (orderedquantity.resource_id = resource.id) GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto AS SELECT internal.*, location.number AS locationNumber, location.name AS locationName, product.number AS productNumber, product.name AS productName, product.unit AS productUnit FROM materialflowresources_warehousestocklistdto_internal internal JOIN materialflow_location location ON (location.id = internal.location_id) JOIN basic_product product ON (product.id = internal.product_id);

-- end

DROP TABLE IF EXISTS ordersupplies_materialrequirementcoveragedto;

CREATE OR REPLACE VIEW ordersupplies_materialrequirementcoveragedto AS SELECT id, number, coveragetodate, actualdate, generateddate, generatedby FROM ordersupplies_materialrequirementcoverage WHERE saved = true;

DROP TABLE IF EXISTS jointable_coverageorderhelper_orderdto;

DROP TABLE IF EXISTS ordersupplies_orderdto;

CREATE OR REPLACE VIEW ordersupplies_orderdto AS SELECT id, number, name, state FROM orders_order;

CREATE TABLE jointable_coverageorderhelper_orderdto (coverageorderhelper_id bigint NOT NULL, orderdto_id bigint NOT NULL, CONSTRAINT jointable_coverageorderhelper_orderdto_pkey PRIMARY KEY (coverageorderhelper_id, orderdto_id), CONSTRAINT jointable_coverageorderhelper_coverageorderhelper_fkey FOREIGN KEY (coverageorderhelper_id) REFERENCES ordersupplies_coverageorderhelper (id) DEFERRABLE);

---
DROP TABLE IF EXISTS orders_orderPlanningListDto;

CREATE OR REPLACE VIEW orders_orderPlanningListDto AS SELECT o.id, o.active, o.number, o.name, o.dateFrom, o.dateTo, o.startDate, o.finishDate, o.state, o.externalNumber, o.externalSynchronized, o.isSubcontracted, o.plannedQuantity, o.workPlanDelivered, product.number AS productNumber, tech.number AS technologyNumber, product.unit, line.number AS productionLineNumber, master.number AS masterOrderNumber, division.name AS divisionName FROM orders_order o JOIN basic_product product ON (o.product_id = product.id) LEFT JOIN technologies_technology tech ON (o.technology_id = tech.id) JOIN productionLines_productionLine line ON (o.productionline_id = line.id) LEFT JOIN masterOrders_masterOrder  master ON (o.masterorder_id = master.id) LEFT JOIN basic_division division ON (tech.division_id = division.id)
--- end

-- QCADOOCLS-4341
DROP TABLE IF EXISTS orders_orderlistdto;

CREATE OR REPLACE VIEW orders_orderlistdto AS SELECT o.id, o.active, o.number, o.name,  o.datefrom,  o.dateto, o.startdate,  o.finishdate, o.state, o.externalnumber, o.externalsynchronized, o.issubcontracted,  o.plannedquantity, o.workplandelivered,  o.deadline,  product.number AS productnumber,  tech.number AS technologynumber,  product.unit,  master.number AS masterordernumber, division.name AS divisionname,  company.name AS companyname, masterdefinition.number AS masterorderdefinitionnumber FROM orders_order o JOIN basic_product product ON o.product_id = product.id LEFT JOIN technologies_technology tech ON o.technology_id = tech.id LEFT JOIN basic_company company ON o.company_id = company.id LEFT JOIN masterorders_masterorder master ON o.masterorder_id = master.id LEFT JOIN masterorders_masterorderdefinition masterdefinition ON master.masterorderdefinition_id = masterdefinition.id LEFT JOIN basic_division division ON tech.division_id = division.id;
-- end

-- subassemblies view
DROP TABLE IF EXISTS basic_subassemblylistdto;

CREATE OR REPLACE VIEW basic_subassemblyListDto AS SELECT s.id, s.active, s.number, s.name, workstation.number AS workstationNumber, workstationType.number AS workstationTypeNumber, date(s.productionDate) AS productionDate, date(event.maxDate) AS lastRepairsDate FROM basic_subassembly s LEFT JOIN basic_workstation workstation ON (s.workstation_id = workstation.id) JOIN basic_workstationType workstationType ON (s.workstationtype_id = workstationType.id) LEFT JOIN ( SELECT subassembly_id AS subassemblyId, MAX(date) AS maxDate FROM cmmsmachineparts_plannedevent e WHERE e.state = '05realized' AND e.basedon = '01date' AND e.type = '02repairs' GROUP BY subassemblyId ) event ON event.subassemblyId = s.id;
-- end

-- pallet terminal

DROP TABLE IF EXISTS goodfood_palletdto;

CREATE OR REPLACE VIEW goodfood_palletdto AS SELECT pallet.id AS id, staff.name AS palletContextOperatorName, staff.surname AS palletContextOperatorSurname, productionline.number AS productionLineNumber, masterorder.number AS masterOrderNumber, product.number AS productNumber, pallet.registrationDate AS registrationDate, pallet.state AS state, pallet.ssccNumber AS ssccNumber, secondPallet.palletNumber AS secondPalletNumber, pallet.lastStateChangeFails AS lastStateChangeFails, pallet.active AS active, pallet.palletNumber AS palletNumber FROM goodfood_pallet pallet LEFT JOIN goodfood_palletcontext palletcontext ON pallet.palletcontext_id = palletcontext.id LEFT JOIN basic_staff staff ON palletcontext.operator_id = staff.id LEFT JOIN goodfood_label label ON pallet.label_id = label.id LEFT JOIN productionlines_productionline productionline ON label.productionline_id = productionline.id LEFT JOIN masterorders_masterorder masterorder ON label.masterorder_id = masterorder.id LEFT JOIN basic_product product ON masterorder.product_id = product.id LEFT JOIN goodfood_pallet secondPallet ON pallet.secondpallet_id = secondPallet.id;

DROP TABLE IF EXISTS goodfood_labeldto;

CREATE OR REPLACE VIEW goodfood_labeldto AS SELECT label.id AS id, staff.name AS palletContextOperatorName, staff.surname AS palletContextOperatorSurname, productionline.number AS productionLineNumber, masterorder.number AS masterOrderNumber, product.number AS productNumber, label.registrationDate AS registrationDate, label.state AS state, label.lastSsccNumber AS lastSsccNumber, label.active AS active FROM goodfood_label label LEFT JOIN goodfood_palletcontext palletcontext ON label.palletcontext_id = palletcontext.id LEFT JOIN basic_staff staff ON palletcontext.operator_id = staff.id LEFT JOIN productionlines_productionline productionline ON label.productionline_id = productionline.id LEFT JOIN masterorders_masterorder masterorder ON label.masterorder_id = masterorder.id LEFT JOIN basic_product product ON masterorder.product_id = product.id;
-- end

-- events views
DROP TABLE IF EXISTS cmmsmachineparts_plannedEventListDto;

CREATE OR REPLACE VIEW cmmsmachineparts_plannedEventListDto AS SELECT e.id, e.number, e.type, owner.name || ' ' || owner.surname  AS ownerName, e.description, factory.number AS factoryNumber, factory.id AS factory_id, division.number AS divisionNumber, division.id AS division_id, productionLine.number AS productionLineNumber, workstation.number AS workstationNumber, subassembly.number AS subassemblyNumber, e.date, e.counter, e.createUser, e.createDate, e.state, context.id AS plannedEventContext_id FROM cmmsmachineparts_plannedevent e LEFT JOIN basic_staff owner ON (e.owner_id = owner.id) JOIN basic_factory factory ON (e.factory_id = factory.id) JOIN basic_division division ON (e.division_id = division.id) LEFT JOIN productionLines_productionLine productionLine ON (e.productionline_id = productionLine.id) LEFT JOIN basic_workstation workstation ON (e.workstation_id = workstation.id) LEFT JOIN basic_subassembly subassembly ON (e.subassembly_id = subassembly.id) LEFT JOIN cmmsmachineparts_plannedeventcontext context ON (e.plannedeventcontext_id = context.id);

DROP TABLE IF EXISTS cmmsmachineparts_maintenanceEventListDto;

CREATE OR REPLACE VIEW cmmsmachineparts_maintenanceEventListDto AS SELECT e.id, e.number, e.type, staff.name || ' ' || staff.surname  AS personReceivingName, e.description, faultType.name AS faultTypeNumber, factory.number AS factoryNumber, division.number AS divisionNumber, factory.id AS factory_id, division.id AS division_id, productionLine.number AS productionLineNumber, workstation.number AS workstationNumber, subassembly.number AS subassemblyNumber, e.createUser, e.createDate, e.state, context.id AS maintenanceEventContext_id FROM cmmsmachineparts_maintenanceevent e LEFT JOIN basic_staff staff ON (e.personreceiving_id = staff.id) JOIN cmmsmachineparts_faulttype faultType ON (e.faulttype_id = faultType.id) JOIN basic_factory factory ON (e.factory_id = factory.id) JOIN basic_division division ON (e.division_id = division.id) LEFT JOIN productionLines_productionLine productionLine ON (e.productionline_id = productionLine.id) LEFT JOIN basic_workstation workstation ON (e.workstation_id = workstation.id) LEFT JOIN basic_subassembly subassembly ON (e.subassembly_id = subassembly.id) LEFT JOIN cmmsmachineparts_maintenanceeventcontext context ON (e.maintenanceeventcontext_id = context.id);

-- end

-- production tracking

DROP TABLE IF EXISTS productioncounting_productiontrackingdto;

CREATE OR REPLACE VIEW productioncounting_productiontrackingdto AS SELECT productiontracking.id::integer AS id, productiontracking.number AS productiontrackingnumber, productiontracking.state AS productiontrackingstate, productiontracking.createdate AS productiontrackingcreatedate, productiontracking.lasttracking AS productiontrackinglasttracking, productiontracking.timerangefrom AS productiontrackingtimerangefrom, productiontracking.timerangeto AS productiontrackingtimerangeto, productiontracking.active AS active, ordersorder.id::integer AS order_id, ordersorder.number AS ordernumber, ordersorder.state AS orderstate, technologyoperationcomponent.id::integer AS technologyoperationcomponent_id, (CASE WHEN technologyoperationcomponent IS NULL THEN '' ELSE (technologyoperationcomponent.nodenumber::text || ' '::text) || operation.name::text END) AS technologyoperationcomponentnumber, operation.id::integer AS operation_id, shift.id::integer AS shift_id, shift.name AS shiftname, staff.id::integer AS staff_id, staff.name || ' ' || staff.surname AS staffname, division.id::integer AS division_id, division.number AS divisionnumber, subcontractor.id AS subcontractor_id, subcontractor.name AS subcontractorname FROM productioncounting_productiontracking productiontracking LEFT JOIN orders_order ordersorder ON ordersorder.id = productiontracking.order_id LEFT JOIN technologies_technologyoperationcomponent technologyoperationcomponent ON technologyoperationcomponent.id = productiontracking.technologyoperationcomponent_id LEFT JOIN technologies_operation operation ON operation.id = technologyoperationcomponent.operation_id LEFT JOIN basic_shift shift ON shift.id = productiontracking.shift_id LEFT JOIN basic_staff staff ON staff.id = productiontracking.staff_id LEFT JOIN basic_division division ON division.id = productiontracking.division_id LEFT JOIN basic_company subcontractor ON subcontractor.id = productiontracking.subcontractor_id;

-- end
