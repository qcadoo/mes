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

CREATE OR REPLACE FUNCTION create_warehouse_stock_view() RETURNS VOID AS $$ BEGIN IF EXISTS (SELECT * FROM information_schema.columns WHERE table_name = 'basic_parameter' AND column_name = 'tenantid') THEN CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity), 0::numeric) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_delivery.active = true AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state in ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity, tenantid FROM materialflowresources_resource GROUP BY tenantid, location_id, product_id; ELSE CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity),0) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.active = true AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state in ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id; END IF; END; $$ LANGUAGE 'plpgsql';

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
CREATE OR REPLACE VIEW orders_orderPlanningListDto as select o.id, o.active, o.number, o.name, o.dateFrom, o.dateTo, o.startDate, o.finishDate, o.state, o.externalNumber, o.externalSynchronized, o.isSubcontracted, o.plannedQuantity, o.workPlanDelivered, product.number as productNumber, tech.number as technologyNumber, product.unit, line.number as productionLineNumber, master.number as masterOrderNumber, division.name as divisionName from orders_order o join basic_product product on (o.product_id = product.id) left join technologies_technology tech on (o.technology_id = tech.id) join productionLines_productionLine line on (o.productionline_id = line.id) left join masterOrders_masterOrder  master on (o.masterorder_id = master.id) left join basic_division division on (tech.division_id = division.id)
--- end

-- QCADOOCLS-4341
DROP TABLE IF EXISTS orders_orderlistdto;
CREATE OR REPLACE VIEW orders_orderlistdto AS SELECT o.id, o.active, o.number, o.name,  o.datefrom,  o.dateto, o.startdate,  o.finishdate, o.state, o.externalnumber, o.externalsynchronized, o.issubcontracted,  o.plannedquantity, o.workplandelivered,  o.deadline,  product.number AS productnumber,  tech.number AS technologynumber,  product.unit,  master.number AS masterordernumber, division.name AS divisionname,  company.name AS companyname, masterdefinition.number AS masterorderdefinitionnumber FROM orders_order o JOIN basic_product product ON o.product_id = product.id LEFT JOIN technologies_technology tech ON o.technology_id = tech.id LEFT JOIN basic_company company ON o.company_id = company.id LEFT JOIN masterorders_masterorder master ON o.masterorder_id = master.id LEFT JOIN masterorders_masterorderdefinition masterdefinition ON master.masterorderdefinition_id = masterdefinition.id LEFT JOIN basic_division division ON tech.division_id = division.id;
-- end

-- subassemblies view
DROP TABLE IF EXISTS basic_subassemblylistdto;
create or replace view basic_subassemblyListDto as select s.id, s.active, s.number, s.name, workstation.number as workstationNumber, workstationType.number as workstationTypeNumber, date(s.productionDate) as productionDate, date(event.maxDate) as lastRepairsDate from basic_subassembly s left join basic_workstation workstation on (s.workstation_id = workstation.id) join basic_workstationType workstationType on (s.workstationtype_id = workstationType.id) left join ( select subassembly_id as subassemblyId, max(date) as maxDate from cmmsmachineparts_plannedevent e where e.state = '05realized' and e.basedon = '01date' and e.type = '02repairs' group by subassemblyId ) event on event.subassemblyId = s.id;
-- end

-- events views
DROP TABLE IF EXISTS cmmsmachineparts_plannedEventListDto;
create or replace view cmmsmachineparts_plannedEventListDto as select e.id, e.number, e.type, concat_ws(' ', owner.name::text, owner.surname::text) as ownerName, e.description, factory.number as factoryNumber, division.number as divisionNumber, productionLine.number as productionLineNumber, workstation.number as workstationNumber, subassembly.number as subassemblyNumber, e.date, e.counter, e.createUser, e.createDate, e.state, context.id as plannedEventContext_id from cmmsmachineparts_plannedevent e left join basic_staff owner on (e.owner_id = owner.id) join basic_factory factory on (e.factory_id = factory.id) join basic_division division on (e.division_id = division.id) left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id) left join basic_workstation workstation on (e.workstation_id = workstation.id) left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id) left join cmmsmachineparts_plannedeventcontext context on (e.plannedeventcontext_id = context.id);

DROP TABLE IF EXISTS cmmsmachineparts_maintenanceEventListDto;
create or replace view cmmsmachineparts_maintenanceEventListDto as select e.id, e.number, e.type, concat_ws(' ', staff.name::text, staff.surname::text) as personReceivingName, e.description, faultType.number as faultTypeNumber, factory.number as factoryNumber, division.number as divisionNumber, productionLine.number as productionLineNumber, workstation.number as workstationNumber, subassembly.number as subassemblyNumber, e.createUser, e.createDate, e.state, context.id as maintenanceEventContext_id from cmmsmachineparts_maintenanceevent e left join basic_staff staff on (e.personreceiving_id = staff.id) join cmmsmachineparts_faulttype faultType on (e.faulttype_id = faultType.id) join basic_factory factory on (e.factory_id = factory.id) join basic_division division on (e.division_id = division.id) left join productionLines_productionLine productionLine on (e.productionline_id = productionLine.id) left join basic_workstation workstation on (e.workstation_id = workstation.id) left join basic_subassembly subassembly on (e.subassembly_id = subassembly.id) left join cmmsmachineparts_maintenanceeventcontext context on (e.maintenanceeventcontext_id = context.id);
-- end