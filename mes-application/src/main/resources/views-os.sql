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
CREATE OR REPLACE FUNCTION drop_all_sequence() RETURNS VOID AS $$  DECLARE ROW record;  BEGIN FOR ROW IN SELECT tablename, substring(quote_ident(tablename) || '_id_seq' from 1 for 63) as seq_name FROM pg_tables p INNER JOIN information_schema.columns c ON p.tablename = c.table_name WHERE c.table_schema = 'public' AND p.schemaname = 'public' AND c.column_name = 'id' AND data_type = 'bigint' LOOP EXECUTE 'ALTER TABLE ' || quote_ident(ROW.tablename) || ' ALTER COLUMN id DROP DEFAULT;';  END LOOP; FOR ROW IN (SELECT c.relname FROM pg_class c WHERE c.relkind = 'S') LOOP EXECUTE 'DROP SEQUENCE ' || row.relname || ';'; END LOOP; END; $$ LANGUAGE 'plpgsql'; 
SELECT * FROM drop_all_sequence();
DROP FUNCTION drop_all_sequence();

CREATE OR REPLACE FUNCTION update_sequence() RETURNS VOID AS $$ DECLARE ROW record; BEGIN FOR ROW IN SELECT tablename, substring(quote_ident(tablename) || '_id_seq' from 1 for 63) as seq_name FROM pg_tables p INNER JOIN information_schema.columns c ON p.tablename = c.table_name WHERE c.table_schema = 'public' AND p.schemaname = 'public' AND c.column_name = 'id' AND data_type = 'bigint' LOOP 	      EXECUTE 'CREATE SEQUENCE ' || ROW.seq_name; EXECUTE 'ALTER TABLE ' || quote_ident(ROW.tablename) || ' ALTER COLUMN id SET DEFAULT nextval('''|| ROW.seq_name||''');';  EXECUTE 'SELECT setval(''' || ROW.seq_name || ''', COALESCE((SELECT MAX(id)+1 FROM ' || quote_ident(ROW.tablename) || '), 1), false);';  END LOOP; END; $$ LANGUAGE 'plpgsql';
SELECT * FROM update_sequence();
DROP FUNCTION update_sequence();

DROP TABLE IF EXISTS materialflowresources_warehousestock;
CREATE OR REPLACE FUNCTION create_warehouse_stock_view() RETURNS VOID AS $$ BEGIN IF EXISTS (SELECT * FROM information_schema.columns WHERE table_name = 'basic_parameter' AND column_name = 'tenantid') THEN CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity), 0::numeric) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_delivery.active = true AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity, tenantid FROM materialflowresources_resource GROUP BY tenantid, location_id, product_id; ELSE CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity),0) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.active = true AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id; END IF; END; $$ LANGUAGE 'plpgsql';
SELECT create_warehouse_stock_view();
DROP FUNCTION create_warehouse_stock_view();

-- optymalizacja QCADOOCLS-4315

DROP TABLE IF EXISTS materialflowresources_warehousestocklistdto;

CREATE OR REPLACE VIEW materialflowresources_orderedquantity AS SELECT COALESCE(SUM(orderedproduct.orderedquantity), 0::numeric) AS orderedquantity, resource.id AS resource_id FROM materialflowresources_resource resource JOIN deliveries_orderedproduct orderedproduct ON (orderedproduct.product_id = resource.product_id) JOIN deliveries_delivery delivery ON (orderedproduct.delivery_id = delivery.id AND delivery.active = true AND delivery.location_id = resource.location_id AND (delivery.state::text = ANY (ARRAY['01draft'::character varying::text, '02prepared'::character varying::text, '03duringCorrection'::character varying::text, '05approved'::character varying::text]))) GROUP BY resource.id;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto_internal AS SELECT row_number() OVER () AS id, resource.location_id, resource.product_id::integer, SUM(resource.quantity) AS quantity, COALESCE(orderedquantity.orderedquantity, 0::numeric) AS orderedquantity, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id) AS minimumstate FROM materialflowresources_resource resource LEFT JOIN materialflowresources_orderedquantity orderedquantity ON (orderedquantity.resource_id = resource.id) GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity;

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto AS SELECT internal.*, location.number AS locationNumber, location.name AS locationName, product.number AS productNumber, product.name AS productName, product.unit AS productUnit FROM materialflowresources_warehousestocklistdto_internal internal JOIN materialflow_location location ON (location.id = internal.location_id) JOIN basic_product product ON (product.id = internal.product_id);

-- end


---

ALTER TABLE orders_order ADD COLUMN issubcontracted boolean;
ALTER TABLE orders_order ALTER COLUMN issubcontracted SET DEFAULT false;
ALTER TABLE technologies_technology ADD COLUMN division_id bigint;
ALTER TABLE technologies_technology ADD CONSTRAINT technology_division_fkey FOREIGN KEY (division_id) REFERENCES basic_division (id) DEFERRABLE;

DROP TABLE IF EXISTS orders_orderPlanningListDto;

CREATE OR REPLACE VIEW orders_orderPlanningListDto AS SELECT o.id, o.active, o.number, o.name, o.dateFrom, o.dateTo, o.startDate, o.finishDate, o.state, o.externalNumber, o.externalSynchronized, o.isSubcontracted, o.plannedQuantity, o.workPlanDelivered, product.number AS productNumber, tech.number AS technologyNumber, product.unit, line.number AS productionLineNumber, master.number AS masterOrderNumber, division.name AS divisionName FROM orders_order o JOIN basic_product product ON (o.product_id = product.id) LEFT JOIN technologies_technology tech ON (o.technology_id = tech.id) JOIN productionLines_productionLine line ON (o.productionline_id = line.id) LEFT JOIN masterOrders_masterOrder  master ON (o.masterorder_id = master.id) LEFT JOIN basic_division division ON (tech.division_id = division.id)

-- end


-- QCADOOCLS-4341

DROP TABLE IF EXISTS orders_orderlistdto;

CREATE OR REPLACE VIEW orders_orderlistdto AS SELECT o.id, o.active, o.number, o.name,  o.datefrom,  o.dateto, o.startdate,  o.finishdate, o.state, o.externalnumber, o.externalsynchronized, o.issubcontracted,  o.plannedquantity, o.workplandelivered,  o.deadline,  product.number AS productnumber,  tech.number AS technologynumber,  product.unit,  master.number AS masterordernumber, division.name AS divisionname,  company.name AS companyname, masterdefinition.number AS masterorderdefinitionnumber FROM orders_order o JOIN basic_product product ON o.product_id = product.id LEFT JOIN technologies_technology tech ON o.technology_id = tech.id LEFT JOIN basic_company company ON o.company_id = company.id LEFT JOIN masterorders_masterorder master ON o.masterorder_id = master.id LEFT JOIN masterorders_masterorderdefinition masterdefinition ON master.masterorderdefinition_id = masterdefinition.id LEFT JOIN basic_division division ON tech.division_id = division.id;

-- end


-- subassemblies view

DROP TABLE IF EXISTS basic_subassemblylistdto;

CREATE OR REPLACE VIEW basic_subassemblyListDto AS SELECT s.id, s.active, s.number, s.name, workstation.number AS workstationNumber, s.type, workstationType.number AS workstationTypeNumber, date(s.productionDate) AS productionDate, date(event.maxDate) AS lastRepairsDate FROM basic_subassembly s LEFT JOIN basic_workstation workstation ON (s.workstation_id = workstation.id) JOIN basic_workstationType workstationType ON (s.workstationtype_id = workstationType.id) LEFT JOIN ( SELECT subassembly_id AS subassemblyId, MAX(date) AS maxDate FROM cmmsmachineparts_plannedevent e WHERE e.state = '05realized' AND e.basedon = '01date' AND e.type = '02repairs' GROUP BY subassemblyId ) event ON event.subassemblyId = s.id;

-- end


-- events views

DROP TABLE IF EXISTS cmmsmachineparts_plannedEventListDto;

CREATE OR REPLACE VIEW cmmsmachineparts_plannedEventListDto AS SELECT e.id, e.number, e.type, owner.name || ' ' || owner.surname  AS ownerName, e.description, factory.number AS factoryNumber, factory.id AS factory_id, division.number AS divisionNumber, division.id AS division_id, productionLine.number AS productionLineNumber, workstation.number AS workstationNumber, subassembly.number AS subassemblyNumber, e.date, e.counter, e.createUser, e.createDate, e.state, context.id AS plannedEventContext_id FROM cmmsmachineparts_plannedevent e LEFT JOIN basic_staff owner ON (e.owner_id = owner.id) JOIN basic_factory factory ON (e.factory_id = factory.id) JOIN basic_division division ON (e.division_id = division.id) LEFT JOIN productionLines_productionLine productionLine ON (e.productionline_id = productionLine.id) LEFT JOIN basic_workstation workstation ON (e.workstation_id = workstation.id) LEFT JOIN basic_subassembly subassembly ON (e.subassembly_id = subassembly.id) LEFT JOIN cmmsmachineparts_plannedeventcontext context ON (e.plannedeventcontext_id = context.id);


DROP TABLE IF EXISTS cmmsmachineparts_maintenanceEventListDto;

CREATE OR REPLACE VIEW cmmsmachineparts_maintenanceEventListDto AS SELECT e.id, e.number, e.type, staff.name || ' ' || staff.surname  AS personReceivingName, e.description, faultType.name AS faultTypeNumber, factory.number AS factoryNumber, division.number AS divisionNumber, factory.id AS factory_id, division.id AS division_id, productionLine.number AS productionLineNumber, workstation.number AS workstationNumber, subassembly.number AS subassemblyNumber, e.createUser, e.createDate, e.state, context.id AS maintenanceEventContext_id FROM cmmsmachineparts_maintenanceevent e LEFT JOIN basic_staff staff ON (e.personreceiving_id = staff.id) JOIN cmmsmachineparts_faulttype faultType ON (e.faulttype_id = faultType.id) JOIN basic_factory factory ON (e.factory_id = factory.id) JOIN basic_division division ON (e.division_id = division.id) LEFT JOIN productionLines_productionLine productionLine ON (e.productionline_id = productionLine.id) LEFT JOIN basic_workstation workstation ON (e.workstation_id = workstation.id) LEFT JOIN basic_subassembly subassembly ON (e.subassembly_id = subassembly.id) LEFT JOIN cmmsmachineparts_maintenanceeventcontext context ON (e.maintenanceeventcontext_id = context.id);

-- end


-- production tracking

ALTER TABLE productioncounting_trackingoperationproductincomponent ADD COLUMN batch_id bigint;
ALTER TABLE productioncounting_trackingoperationproductoutcomponent ADD COLUMN batch_id bigint;

DROP TABLE IF EXISTS productioncounting_productiontrackingdto;

CREATE OR REPLACE VIEW productioncounting_productiontrackingdto AS SELECT productiontracking.id AS id, productiontracking.number AS number, productiontracking.state AS state, productiontracking.createdate AS createdate, productiontracking.lasttracking AS lasttracking, productiontracking.timerangefrom AS timerangefrom, productiontracking.timerangeto AS timerangeto, productiontracking.active AS active, ordersorder.id::integer AS order_id, ordersorder.number AS ordernumber, ordersorder.state AS orderstate, technologyoperationcomponent.id::integer AS technologyoperationcomponent_id, (CASE WHEN technologyoperationcomponent IS NULL THEN '' ELSE (technologyoperationcomponent.nodenumber::text || ' '::text) || operation.name::text END) AS technologyoperationcomponentnumber, operation.id::integer AS operation_id, shift.id::integer AS shift_id, shift.name AS shiftname, staff.id::integer AS staff_id, staff.name || ' ' || staff.surname AS staffname, division.id::integer AS division_id, division.number AS divisionnumber, subcontractor.id::integer AS subcontractor_id, subcontractor.name AS subcontractorname FROM productioncounting_productiontracking productiontracking LEFT JOIN orders_order ordersorder ON ordersorder.id = productiontracking.order_id LEFT JOIN technologies_technologyoperationcomponent technologyoperationcomponent ON technologyoperationcomponent.id = productiontracking.technologyoperationcomponent_id LEFT JOIN technologies_operation operation ON operation.id = technologyoperationcomponent.operation_id LEFT JOIN basic_shift shift ON shift.id = productiontracking.shift_id LEFT JOIN basic_staff staff ON staff.id = productiontracking.staff_id LEFT JOIN basic_division division ON division.id = productiontracking.division_id LEFT JOIN basic_company subcontractor ON subcontractor.id = productiontracking.subcontractor_id;


DROP TABLE IF EXISTS productioncounting_trackingoperationproductincomponentdto;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductincomponentdto AS SELECT trackingoperationproductincomponent.id AS id, productiontracking.id::integer AS productiontracking_id, product.id::integer AS product_id, product.number AS productnumber, product.unit AS productunit, CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum) ELSE (SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum) END AS plannedquantity, trackingoperationproductincomponent.usedquantity AS usedquantity, batch.number AS batchnumber FROM productioncounting_trackingoperationproductincomponent trackingoperationproductincomponent LEFT JOIN productioncounting_productiontracking productiontracking ON productiontracking.id = trackingoperationproductincomponent.productiontracking_id LEFT JOIN basic_product product ON product.id = trackingoperationproductincomponent.product_id LEFT JOIN advancedgenealogy_batch batch ON batch.id = trackingoperationproductincomponent.batch_id LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1 ON ( productioncountingquantity_1.order_id = productiontracking.order_id AND productioncountingquantity_1.product_id = trackingoperationproductincomponent.product_id AND productioncountingquantity_1.role::text = '01used'::text ) LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2 ON ( productioncountingquantity_2.order_id = productiontracking.order_id AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id AND productioncountingquantity_2.product_id = trackingoperationproductincomponent.product_id AND productioncountingquantity_2.role::text = '01used'::text ) GROUP BY trackingoperationproductincomponent.id, productiontracking.id, product.id, product.name, product.unit, trackingoperationproductincomponent.usedquantity, productiontracking.technologyoperationcomponent_id, batch.number;


DROP TABLE IF EXISTS productioncounting_trackingoperationproductoutcomponentdto;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductoutcomponentdto AS SELECT trackingoperationproductoutcomponent.id AS id, productiontracking.id::integer AS productiontracking_id, product.id::integer AS product_id, product.number AS productnumber, product.unit AS productunit, CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum) ELSE (SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum) END AS plannedquantity, trackingoperationproductoutcomponent.usedquantity AS usedquantity, batch.number AS batchnumber FROM productioncounting_trackingoperationproductoutcomponent trackingoperationproductoutcomponent LEFT JOIN productioncounting_productiontracking productiontracking ON productiontracking.id = trackingoperationproductoutcomponent.productiontracking_id LEFT JOIN basic_product product ON product.id = trackingoperationproductoutcomponent.product_id LEFT JOIN advancedgenealogy_batch batch ON batch.id = trackingoperationproductoutcomponent.batch_id LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1 ON ( productioncountingquantity_1.order_id = productiontracking.order_id AND productioncountingquantity_1.product_id = trackingoperationproductoutcomponent.product_id AND productioncountingquantity_1.role::text = '02produced'::text ) LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2 ON ( productioncountingquantity_2.order_id = productiontracking.order_id AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id AND productioncountingquantity_2.product_id = trackingoperationproductoutcomponent.product_id AND productioncountingquantity_2.role::text = '02produced'::text ) GROUP BY trackingoperationproductoutcomponent.id, productiontracking.id, product.id, product.number, product.unit, trackingoperationproductoutcomponent.usedquantity, productiontracking.technologyoperationcomponent_id, batch.number;


DROP TABLE IF EXISTS productioncounting_trackingoperationproductcomponentdto;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductcomponentdto AS SELECT row_number() OVER () AS id, trackingoperationproductcomponentdto.productiontracking_id::integer AS productiontracking_id, trackingoperationproductcomponentdto.product_id::integer AS product_id, trackingoperationproductcomponentdto.productnumber AS productnumber, trackingoperationproductcomponentdto.productunit AS productunit, trackingoperationproductcomponentdto.plannedquantity AS plannedquantity, trackingoperationproductcomponentdto.usedquantity AS usedquantity, trackingoperationproductcomponentdto.batchnumber FROM (SELECT trackingoperationproductincomponentdto.productiontracking_id, trackingoperationproductincomponentdto.product_id, trackingoperationproductincomponentdto.productnumber, trackingoperationproductincomponentdto.productunit, trackingoperationproductincomponentdto.plannedquantity, trackingoperationproductincomponentdto.usedquantity, trackingoperationproductincomponentdto.batchnumber FROM productioncounting_trackingoperationproductincomponentdto trackingoperationproductincomponentdto UNION SELECT trackingoperationproductoutcomponentdto.productiontracking_id, trackingoperationproductoutcomponentdto.product_id, trackingoperationproductoutcomponentdto.productnumber, trackingoperationproductoutcomponentdto.productunit, trackingoperationproductoutcomponentdto.plannedquantity, trackingoperationproductoutcomponentdto.usedquantity FROM productioncounting_trackingoperationproductoutcomponentdto trackingoperationproductoutcomponentdto) trackingoperationproductcomponentdto, trackingoperationproductoutcomponentdto.batchnumber;


DROP TABLE IF EXISTS productioncounting_productiontrackingforproductdto;

CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductdto AS SELECT trackingoperationproductcomponentdto.id AS id, productiontrackingdto.number AS number, productiontrackingdto.state AS state, productiontrackingdto.createdate AS createdate, productiontrackingdto.lasttracking AS lasttracking, productiontrackingdto.timerangefrom AS timerangefrom, productiontrackingdto.timerangeto AS timerangeto, productiontrackingdto.active AS active, productiontrackingdto.order_id::integer AS order_id, productiontrackingdto.ordernumber AS ordernumber, productiontrackingdto.orderstate AS orderstate, productiontrackingdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id, productiontrackingdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber, productiontrackingdto.operation_id::integer AS operation_id, productiontrackingdto.shift_id::integer AS shift_id, productiontrackingdto.shiftname AS shiftname, productiontrackingdto.staff_id::integer AS staff_id, productiontrackingdto.staffname AS staffname, productiontrackingdto.division_id::integer AS division_id, productiontrackingdto.divisionnumber AS divisionnumber, productiontrackingdto.subcontractor_id::integer AS subcontractor_id, productiontrackingdto.subcontractorname AS subcontractorname, trackingoperationproductcomponentdto.product_id::integer AS product_id, trackingoperationproductcomponentdto.productnumber AS productnumber, trackingoperationproductcomponentdto.productunit AS productunit, trackingoperationproductcomponentdto.plannedquantity AS plannedquantity, trackingoperationproductcomponentdto.usedquantity AS usedquantity, productiontrackingdto.id AS productiontracking_id, trackingoperationproductcomponentdto.batchnumber FROM productioncounting_trackingoperationproductcomponentdto trackingoperationproductcomponentdto LEFT JOIN productioncounting_productiontrackingdto productiontrackingdto ON productiontrackingdto.id = trackingoperationproductcomponentdto.productiontracking_id;


DROP TABLE IF EXISTS productioncounting_productiontrackingforproductgroupeddto;

CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductgroupeddto AS SELECT row_number() OVER () AS id, productiontrackingforproductdto.active AS active, productiontrackingforproductdto.order_id::integer AS order_id, productiontrackingforproductdto.ordernumber AS ordernumber, productiontrackingforproductdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id, productiontrackingforproductdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber, productiontrackingforproductdto.operation_id AS operation_id, productiontrackingforproductdto.product_id::integer AS product_id, productiontrackingforproductdto.productnumber AS productnumber, productiontrackingforproductdto.productunit AS productunit, productiontrackingforproductdto.plannedquantity AS plannedquantity, SUM(productiontrackingforproductdto.usedquantity) AS usedquantity FROM productioncounting_productiontrackingforproductdto productiontrackingforproductdto GROUP BY productiontrackingforproductdto.active, productiontrackingforproductdto.order_id, productiontrackingforproductdto.ordernumber, productiontrackingforproductdto.technologyoperationcomponent_id, productiontrackingforproductdto.technologyoperationcomponentnumber, productiontrackingforproductdto.operation_id, productiontrackingforproductdto.product_id, productiontrackingforproductdto.productnumber, productiontrackingforproductdto.productunit, productiontrackingforproductdto.plannedquantity;

-- end


-- #QCADOO-432

CREATE OR REPLACE FUNCTION generate_and_set_resource_number(_time timestamp) RETURNS text AS $$ DECLARE _pattern text;_year numeric;_sequence_name text;_sequence_value numeric;_tmp text;_seq text;_number text; BEGIN _pattern := '#year/#seq'; _year := extract(year from _time);_sequence_name := 'materialflowresources_resource_number_' || _year;SELECT sequence_name into _tmp FROM information_schema.sequences where sequence_schema = 'public' and sequence_name = _sequence_name;if _tmp is null then execute 'CREATE SEQUENCE ' || _sequence_name || ';';end if;select nextval(_sequence_name) into _sequence_value;_seq := to_char(_sequence_value, 'fm00000');if _seq like '%#%' then _seq := _sequence_value;end if;_number := _pattern;_number := replace(_number, '#year', _year::text);_number := replace(_number, '#seq', _seq);RETURN _number;END;$$ LANGUAGE 'plpgsql';
CREATE OR REPLACE FUNCTION generate_and_set_resource_number_trigger() RETURNS trigger AS $$ BEGIN NEW.number := generate_and_set_resource_number(NEW.time);return NEW;END;$$ LANGUAGE 'plpgsql';
CREATE TRIGGER materialflowresources_resource_trigger_number BEFORE INSERT ON materialflowresources_resource FOR EACH ROW EXECUTE PROCEDURE generate_and_set_resource_number_trigger();

--end #QCADOO-432

--end #QCADOO-433
CREATE OR REPLACE FUNCTION generate_document_number(_translated_type text) RETURNS text AS $$ DECLARE _pattern text;_sequence_name text;_sequence_value numeric;_tmp text;_seq text; _number text; BEGIN _pattern := '#translated_type/#seq'; _sequence_name := 'materialflowresources_document_number_' || lower(_translated_type); SELECT sequence_name into _tmp FROM information_schema.sequences where sequence_schema = 'public' and sequence_name = _sequence_name; if _tmp is null then execute 'CREATE SEQUENCE ' || _sequence_name || ';'; end if; select nextval(_sequence_name) into _sequence_value; _seq := to_char(_sequence_value, 'fm00000'); if _seq like '%#%' then _seq := _sequence_value; end if; _number := _pattern; _number := replace(_number, '#translated_type', _translated_type); _number := replace(_number, '#seq', _seq); RETURN _number; END; $$ LANGUAGE 'plpgsql';
CREATE OR REPLACE FUNCTION generate_and_set_document_number_trigger() RETURNS trigger AS $$ BEGIN NEW.number := generate_document_number(NEW.number); IF NEW.name is null THEN NEW.name := NEW.number; END IF; return NEW; END; $$ LANGUAGE 'plpgsql';
CREATE TRIGGER materialflowresources_document_trigger_number BEFORE INSERT ON materialflowresources_document FOR EACH ROW EXECUTE PROCEDURE generate_and_set_document_number_trigger();
--end #QCADOO-433

-- #GOODFOOD-1196
CREATE SEQUENCE cmmsmachineparts_maintenanceevent_number_seq;
CREATE OR REPLACE FUNCTION generate_maintenanceevent_number() RETURNS text AS $$ DECLARE _pattern text; _sequence_name text; _sequence_value numeric; _tmp text; _seq text; _number text; BEGIN _pattern := '#seq'; select nextval('cmmsmachineparts_maintenanceevent_number_seq') into _sequence_value; _seq := to_char(_sequence_value, 'fm000000'); if _seq like '%#%' then _seq := _sequence_value; end if;	 _number := _pattern; _number := replace(_number, '#seq', _seq);	 RETURN _number; END; $$ LANGUAGE 'plpgsql';
CREATE OR REPLACE FUNCTION generate_and_set_maintenanceevent_number_trigger() RETURNS trigger AS $$ BEGIN NEW.number := generate_maintenanceevent_number(); return NEW; END; $$ LANGUAGE 'plpgsql';
CREATE TRIGGER cmmsmachineparts_maintenanceevent_trigger_number BEFORE INSERT ON cmmsmachineparts_maintenanceevent FOR EACH ROW EXECUTE PROCEDURE generate_and_set_maintenanceevent_number_trigger();
-- end #GOODFOOD-1196

-- VIEW: technologies_technologydto
DROP TABLE IF EXISTS technologies_technologydto;

CREATE OR REPLACE VIEW technologies_technologydto AS SELECT technology.id, technology.name, technology.number, technology.externalsynchronized, technology.master, technology.state, product.number AS productnumber, product.globaltypeofmaterial AS productglobaltypeofmaterial, tg.number AS technologygroupnumber, division.name AS divisionname, product.name AS productname, technology.technologytype, technology.active FROM technologies_technology technology LEFT JOIN basic_product product ON technology.product_id = product.id LEFT JOIN basic_division division ON technology.division_id = division.id LEFT JOIN technologies_technologygroup tg ON technology.technologygroup_id = tg.id;

-- end

CREATE OR REPLACE FUNCTION prepare_documentpositionparameters() RETURNS VOID AS $$ BEGIN insert into materialflowresources_documentpositionparameters (id) values (1); insert into materialflowresources_documentpositionparametersitem (id,ordering,name, parameters_id, editable) values         (1,1,'act', 1, false),(2,2,'number', 1, false),(3,3,'product', 1, false),(4,4,'additionalCode', 1, true),(5,5,'quantity', 1, false),(6,6,'unit', 1, false),(7,7,'givenquantity', 1, false),(8,8,'givenunit', 1, false),(9,9,'conversion', 1, false),(10,10,'resource', 1, true),(11,11,'price', 1, true),(12,12,'batch', 1, true),(13,13,'productiondate', 1, true),(14,14,'expirationdate', 1, true),(15,15,'storageLocation', 1, true),(16,16,'palletNumber', 1, true),(17,17,'typeOfPallet', 1, true); END; $$ LANGUAGE 'plpgsql'; 
SELECT * FROM prepare_documentpositionparameters();
DROP FUNCTION prepare_documentpositionparameters();
