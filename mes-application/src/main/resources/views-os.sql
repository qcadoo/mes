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

-- This script is invoked when application starts with hbm2ddlAuto = create.

--  Qcadoo-model & Hibernate will automatically generate regular db table due to existence of warehouseStock.xml model,
-- we need to first drop this table, before create table view.

CREATE OR REPLACE FUNCTION drop_all_sequence() RETURNS VOID AS $$ DECLARE ROW record; BEGIN FOR ROW IN SELECT tablename, SUBSTRING(quote_ident(tablename) || '_id_seq' FROM 1 FOR 63) AS seq_name FROM pg_tables p INNER JOIN information_schema.columns c ON p.tablename = c.table_name WHERE c.table_schema = 'public' AND p.schemaname = 'public' AND c.column_name = 'id' AND data_type = 'bigint' LOOP EXECUTE 'ALTER TABLE ' || quote_ident(ROW.tablename) || ' ALTER COLUMN id DROP DEFAULT;';  END LOOP; FOR ROW IN (SELECT c.relname FROM pg_class c WHERE c.relkind = 'S') LOOP EXECUTE 'DROP SEQUENCE ' || row.relname || ';'; END LOOP; END; $$ LANGUAGE 'plpgsql';

SELECT * FROM drop_all_sequence();

DROP FUNCTION drop_all_sequence();


CREATE OR REPLACE FUNCTION update_sequence() RETURNS VOID AS $$ DECLARE ROW record; BEGIN FOR ROW IN SELECT tablename, SUBSTRING(quote_ident(tablename) || '_id_seq' FROM 1 FOR 63) AS seq_name FROM pg_tables p INNER JOIN information_schema.columns c ON p.tablename = c.table_name WHERE c.table_schema = 'public' AND p.schemaname = 'public' AND c.column_name = 'id' AND data_type = 'bigint' LOOP EXECUTE 'CREATE SEQUENCE ' || ROW.seq_name; EXECUTE 'ALTER TABLE ' || quote_ident(ROW.tablename) || ' ALTER COLUMN id SET DEFAULT nextval('''|| ROW.seq_name||''');';  EXECUTE 'SELECT setval(''' || ROW.seq_name || ''', COALESCE((SELECT MAX(id)+1 FROM ' || quote_ident(ROW.tablename) || '), 1), false);';  END LOOP; END; $$ LANGUAGE 'plpgsql';

SELECT * FROM update_sequence();

DROP FUNCTION update_sequence();


DROP TABLE IF EXISTS materialflowresources_warehousestock;

CREATE OR REPLACE FUNCTION create_warehouse_stock_view() RETURNS VOID AS $$ BEGIN IF EXISTS (SELECT * FROM information_schema.columns WHERE table_name = 'basic_parameter' AND column_name = 'tenantid') THEN CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, (SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity), 0::numeric) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_delivery.active = true AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity, tenantid FROM materialflowresources_resource GROUP BY tenantid, location_id, product_id; ELSE CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, (SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity),0) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.active = true AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state IN ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id; END IF; END; $$ LANGUAGE 'plpgsql';

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


DROP TABLE IF EXISTS orders_orderplanninglistdto;

CREATE OR REPLACE VIEW orders_orderplanninglistdto AS SELECT ordersorder.id, ordersorder.active, ordersorder.number, ordersorder.name, ordersorder.datefrom, ordersorder.dateto, ordersorder.startdate, ordersorder.finishdate, ordersorder.state, ordersorder.externalnumber, ordersorder.externalsynchronized, ordersorder.issubcontracted, ordersorder.plannedquantity, ordersorder.workplandelivered, ordersorder.ordercategory, coalesce(ordersorder.amountOfProductProduced, 0) as amountOfProductProduced, coalesce(ordersorder.wastesQuantity, 0) as wastesQuantity, coalesce(ordersorder.remainingAmountOfProductToProduce,0) as remainingAmountOfProductToProduce,product.number AS productnumber, technology.number AS technologynumber, product.unit AS unit, productionline.number AS productionlinenumber, masterorder.number AS masterordernumber, division.name AS divisionname, division.number as divisionnumber FROM orders_order ordersorder JOIN basic_product product ON product.id = ordersorder.product_id LEFT JOIN technologies_technology technology ON technology.id = ordersorder.technology_id LEFT JOIN productionlines_productionline productionline ON productionline.id = ordersorder.productionline_id LEFT JOIN masterorders_masterorder masterorder ON masterorder.id = ordersorder.masterorder_id LEFT JOIN basic_division division ON division.id = technology.division_id;

-- end


-- QCADOOCLS-4341

DROP TABLE IF EXISTS orders_orderlistdto;

CREATE OR REPLACE VIEW orders_orderlistdto AS SELECT o.id, o.active, o.number, o.name,  o.datefrom,  o.dateto, o.startdate,  o.finishdate, o.state, o.externalnumber, o.externalsynchronized, o.issubcontracted,  o.plannedquantity, o.workplandelivered,  o.deadline,  product.number AS productnumber,  tech.number AS technologynumber,  product.unit,  master.number AS masterordernumber, division.name AS divisionname,  company.name AS companyname, masterdefinition.number AS masterorderdefinitionnumber FROM orders_order o JOIN basic_product product ON o.product_id = product.id LEFT JOIN technologies_technology tech ON o.technology_id = tech.id LEFT JOIN basic_company company ON o.company_id = company.id LEFT JOIN masterorders_masterorder master ON o.masterorder_id = master.id LEFT JOIN masterorders_masterorderdefinition masterdefinition ON master.masterorderdefinition_id = masterdefinition.id LEFT JOIN basic_division division ON tech.division_id = division.id;

-- end


-- subassemblies view

DROP TABLE IF EXISTS basic_subassemblylistdto;

CREATE OR REPLACE VIEW basic_subassemblyListDto AS SELECT s.id, s.active, s.number, s.name, workstation.number AS workstationNumber, s.type, workstationType.number AS workstationTypeNumber, DATE(s.productionDate) AS productionDate, DATE(event.maxDate) AS lastRepairsDate FROM basic_subassembly s LEFT JOIN basic_workstation workstation ON (s.workstation_id = workstation.id) JOIN basic_workstationType workstationType ON (s.workstationtype_id = workstationType.id) LEFT JOIN (SELECT subassembly_id AS subassemblyId, MAX(date) AS maxDate FROM cmmsmachineparts_plannedevent e WHERE e.state = '05realized' AND e.basedon = '01date' AND e.type = '02repairs' GROUP BY subassemblyId) event ON event.subassemblyId = s.id;

-- end


-- events views

DROP TABLE IF EXISTS cmmsmachineparts_plannedeventlistdto;

CREATE OR REPLACE VIEW cmmsmachineparts_plannedeventlistdto AS SELECT plannedevent.id AS id, plannedevent.number AS number, plannedevent.type AS type, plannedevent.description AS description, plannedevent.date::TIMESTAMP WITHOUT TIME ZONE AS date, plannedevent.counter AS counter, plannedevent.createUser AS createuser, plannedevent.createDate AS createdate, plannedevent.state AS state, context.id::integer AS plannedeventcontext_id, sourcecost.id AS sourcecost_id, staff.name || ' ' || staff.surname AS ownername, factory.id::integer AS factory_id, factory.number AS factorynumber, division.id::integer AS division_id, division.number AS divisionnumber, workstation.id::integer AS workstation_id, workstation.number AS workstationnumber, subassembly.id::integer AS subassembly_id, subassembly.number AS subassemblynumber, company.id::integer AS company_id, productionline.number AS productionlinenumber FROM cmmsmachineparts_plannedevent plannedevent LEFT JOIN cmmsmachineparts_plannedeventcontext context ON plannedevent.plannedeventcontext_id = context.id LEFT JOIN cmmsmachineparts_sourcecost sourcecost ON plannedevent.sourcecost_id = sourcecost.id LEFT JOIN basic_staff staff ON plannedevent.owner_id = staff.id LEFT JOIN basic_factory factory ON plannedevent.factory_id = factory.id LEFT JOIN basic_division division ON plannedevent.division_id = division.id LEFT JOIN basic_workstation workstation ON plannedevent.workstation_id = workstation.id LEFT JOIN basic_subassembly subassembly ON plannedevent.subassembly_id = subassembly.id LEFT JOIN basic_company company ON plannedevent.company_id = company.id LEFT JOIN productionlines_productionline productionline ON plannedevent.productionline_id = productionline.id;


DROP TABLE IF EXISTS cmmsmachineparts_maintenanceeventlistdto;

CREATE OR REPLACE VIEW cmmsmachineparts_maintenanceeventlistdto AS SELECT maintenanceevent.id AS id, maintenanceevent.number AS number, maintenanceevent.type AS type, maintenanceevent.createuser AS createuser, maintenanceevent.createdate AS createdate, maintenanceevent.state AS state, maintenanceevent.description AS description, context.id::integer AS maintenanceeventcontext_id, staff.name || ' ' || staff.surname AS personreceivingname, factory.id::integer AS factory_id, factory.number AS factorynumber, division.id::integer AS division_id, division.number AS divisionnumber, workstation.number AS workstationnumber, subassembly.number AS subassemblynumber, faultType.name AS faulttypename, productionline.number AS productionlinenumber FROM cmmsmachineparts_maintenanceevent maintenanceevent LEFT JOIN cmmsmachineparts_maintenanceeventcontext context ON maintenanceevent.maintenanceeventcontext_id = context.id LEFT JOIN basic_staff staff ON maintenanceevent.personreceiving_id = staff.id LEFT JOIN basic_factory factory ON maintenanceevent.factory_id = factory.id LEFT JOIN basic_division division ON maintenanceevent.division_id = division.id LEFT JOIN basic_workstation workstation ON maintenanceevent.workstation_id = workstation.id LEFT JOIN basic_subassembly subassembly ON maintenanceevent.subassembly_id = subassembly.id LEFT JOIN basic_faulttype faultType ON maintenanceevent.faulttype_id = faultType.id LEFT JOIN productionlines_productionline productionline ON maintenanceevent.productionline_id = productionline.id;

-- end


-- production tracking

DROP TABLE IF EXISTS productioncounting_productiontrackingdto;

CREATE OR REPLACE VIEW productioncounting_productiontrackingdto AS SELECT productiontracking.id AS id, productiontracking.number AS number, productiontracking.state AS state, productiontracking.createdate AS createdate, productiontracking.lasttracking AS lasttracking, productiontracking.timerangefrom AS timerangefrom, productiontracking.timerangeto AS timerangeto, productiontracking.active AS active, ordersorder.id::integer AS order_id, ordersorder.number AS ordernumber, ordersorder.state AS orderstate, technologyoperationcomponent.id::integer AS technologyoperationcomponent_id, (CASE WHEN technologyoperationcomponent IS NULL THEN '' ELSE (technologyoperationcomponent.nodenumber::text || ' '::text) || operation.name::text END) AS technologyoperationcomponentnumber, operation.id::integer AS operation_id, shift.id::integer AS shift_id, shift.name AS shiftname, staff.id::integer AS staff_id, staff.name || ' ' || staff.surname AS staffname, division.id::integer AS division_id, division.number AS divisionnumber, subcontractor.id::integer AS subcontractor_id, subcontractor.name AS subcontractorname, productiontrackingcorrection.number AS correctionnumber, productionline.id::integer AS productionline_id, productionline.number AS productionLineNumber, CONCAT(product.number,' - ',product.name) AS productNumber, product.unit AS productUnit, outcomponent.usedquantity AS usedQuantity FROM productioncounting_productiontracking productiontracking LEFT JOIN orders_order ordersorder ON ordersorder.id = productiontracking.order_id LEFT JOIN productionlines_productionline productionline ON productionline.id = ordersorder.productionline_id LEFT JOIN technologies_technologyoperationcomponent technologyoperationcomponent ON technologyoperationcomponent.id = productiontracking.technologyoperationcomponent_id LEFT JOIN technologies_operation operation ON operation.id = technologyoperationcomponent.operation_id LEFT JOIN basic_shift shift ON shift.id = productiontracking.shift_id LEFT JOIN basic_staff staff ON staff.id = productiontracking.staff_id LEFT JOIN basic_division division ON division.id = productiontracking.division_id LEFT JOIN basic_company subcontractor ON subcontractor.id = productiontracking.subcontractor_id LEFT JOIN productioncounting_productiontracking productiontrackingcorrection ON productiontrackingcorrection.id = productiontracking.correction_id  LEFT JOIN basic_product product ON ordersorder.product_id = product.id LEFT JOIN productioncounting_trackingoperationproductoutcomponent outcomponent ON (outcomponent.product_id = product.id AND productiontracking.id = outcomponent.productiontracking_id);


DROP TABLE IF EXISTS productioncounting_trackingoperationproductincomponentdto;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductincomponentdto AS SELECT trackingoperationproductincomponent.id AS id, productiontracking.id::integer AS productiontracking_id, product.id::integer AS product_id, product.number AS productnumber, product.unit AS productunit, CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum) ELSE (SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum) END AS plannedquantity, trackingoperationproductincomponent.usedquantity AS usedquantity FROM productioncounting_trackingoperationproductincomponent trackingoperationproductincomponent LEFT JOIN productioncounting_productiontracking productiontracking ON productiontracking.id = trackingoperationproductincomponent.productiontracking_id LEFT JOIN basic_product product ON product.id = trackingoperationproductincomponent.product_id LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1 ON (productioncountingquantity_1.order_id = productiontracking.order_id AND productioncountingquantity_1.product_id = trackingoperationproductincomponent.product_id AND productioncountingquantity_1.role::text = '01used'::text) LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2 ON (productioncountingquantity_2.order_id = productiontracking.order_id AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id AND productioncountingquantity_2.product_id = trackingoperationproductincomponent.product_id AND productioncountingquantity_2.role::text = '01used'::text) WHERE productiontracking.state NOT IN ('03declined'::text,'04corrected'::text) GROUP BY trackingoperationproductincomponent.id, productiontracking.id, product.id, product.number, product.unit, trackingoperationproductincomponent.usedquantity, productiontracking.technologyoperationcomponent_id;


DROP TABLE IF EXISTS productioncounting_trackingoperationproductoutcomponentdto;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductoutcomponentdto AS SELECT trackingoperationproductoutcomponent.id AS id, productiontracking.id::integer AS productiontracking_id, product.id::integer AS product_id, product.number AS productnumber, product.unit AS productunit, CASE WHEN productiontracking.technologyoperationcomponent_id IS NULL THEN (SELECT SUM(productioncountingquantity_1.plannedquantity) AS sum) ELSE (SELECT SUM(productioncountingquantity_2.plannedquantity) AS sum) END AS plannedquantity, trackingoperationproductoutcomponent.usedquantity AS usedquantity FROM productioncounting_trackingoperationproductoutcomponent trackingoperationproductoutcomponent LEFT JOIN productioncounting_productiontracking productiontracking ON productiontracking.id = trackingoperationproductoutcomponent.productiontracking_id LEFT JOIN basic_product product ON product.id = trackingoperationproductoutcomponent.product_id LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_1 ON (productioncountingquantity_1.order_id = productiontracking.order_id AND productioncountingquantity_1.product_id = trackingoperationproductoutcomponent.product_id AND productioncountingquantity_1.role::text = '02produced'::text) LEFT JOIN basicproductioncounting_productioncountingquantity productioncountingquantity_2 ON (productioncountingquantity_2.order_id = productiontracking.order_id AND productioncountingquantity_2.technologyoperationcomponent_id = productiontracking.technologyoperationcomponent_id AND productioncountingquantity_2.product_id = trackingoperationproductoutcomponent.product_id AND productioncountingquantity_2.role::text = '02produced'::text) WHERE productiontracking.state NOT IN ('03declined'::text,'04corrected'::text) GROUP BY trackingoperationproductoutcomponent.id, productiontracking.id, product.id, product.number, product.unit, trackingoperationproductoutcomponent.usedquantity, productiontracking.technologyoperationcomponent_id;


DROP TABLE IF EXISTS productioncounting_trackingoperationproductcomponentdto;

CREATE OR REPLACE VIEW productioncounting_trackingoperationproductcomponentdto AS SELECT row_number() OVER () AS id, trackingoperationproductcomponentdto.productiontracking_id::integer AS productiontracking_id, trackingoperationproductcomponentdto.product_id::integer AS product_id, trackingoperationproductcomponentdto.productnumber AS productnumber, trackingoperationproductcomponentdto.productunit AS productunit, trackingoperationproductcomponentdto.plannedquantity AS plannedquantity, trackingoperationproductcomponentdto.usedquantity AS usedquantity FROM (SELECT trackingoperationproductincomponentdto.productiontracking_id, trackingoperationproductincomponentdto.product_id, trackingoperationproductincomponentdto.productnumber, trackingoperationproductincomponentdto.productunit, trackingoperationproductincomponentdto.plannedquantity, trackingoperationproductincomponentdto.usedquantity FROM productioncounting_trackingoperationproductincomponentdto trackingoperationproductincomponentdto UNION SELECT trackingoperationproductoutcomponentdto.productiontracking_id, trackingoperationproductoutcomponentdto.product_id, trackingoperationproductoutcomponentdto.productnumber, trackingoperationproductoutcomponentdto.productunit, trackingoperationproductoutcomponentdto.plannedquantity, trackingoperationproductoutcomponentdto.usedquantity FROM productioncounting_trackingoperationproductoutcomponentdto trackingoperationproductoutcomponentdto) trackingoperationproductcomponentdto;


DROP TABLE IF EXISTS productioncounting_productiontrackingforproductdto;

CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductdto AS SELECT trackingoperationproductcomponentdto.id AS id, productiontrackingdto.number AS number, productiontrackingdto.state AS state, productiontrackingdto.createdate AS createdate, productiontrackingdto.lasttracking AS lasttracking, productiontrackingdto.timerangefrom AS timerangefrom, productiontrackingdto.timerangeto AS timerangeto, productiontrackingdto.active AS active, productiontrackingdto.order_id::integer AS order_id, productiontrackingdto.ordernumber AS ordernumber, productiontrackingdto.orderstate AS orderstate, productiontrackingdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id, productiontrackingdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber, productiontrackingdto.operation_id::integer AS operation_id, productiontrackingdto.shift_id::integer AS shift_id, productiontrackingdto.shiftname AS shiftname, productiontrackingdto.staff_id::integer AS staff_id, productiontrackingdto.staffname AS staffname, productiontrackingdto.division_id::integer AS division_id, productiontrackingdto.divisionnumber AS divisionnumber, productiontrackingdto.subcontractor_id::integer AS subcontractor_id, productiontrackingdto.subcontractorname AS subcontractorname, trackingoperationproductcomponentdto.product_id::integer AS product_id, trackingoperationproductcomponentdto.productnumber AS productnumber, trackingoperationproductcomponentdto.productunit AS productunit, trackingoperationproductcomponentdto.plannedquantity AS plannedquantity, trackingoperationproductcomponentdto.usedquantity AS usedquantity, productiontrackingdto.id::integer AS productiontracking_id FROM productioncounting_trackingoperationproductcomponentdto trackingoperationproductcomponentdto LEFT JOIN productioncounting_productiontrackingdto productiontrackingdto ON productiontrackingdto.id = trackingoperationproductcomponentdto.productiontracking_id;


DROP TABLE IF EXISTS productioncounting_productiontrackingforproductgroupeddto;

CREATE OR REPLACE VIEW productioncounting_productiontrackingforproductgroupeddto AS SELECT row_number() OVER () AS id, productiontrackingforproductdto.active AS active, productiontrackingforproductdto.order_id::integer AS order_id, productiontrackingforproductdto.ordernumber AS ordernumber, productiontrackingforproductdto.technologyoperationcomponent_id::integer AS technologyoperationcomponent_id, productiontrackingforproductdto.technologyoperationcomponentnumber AS technologyoperationcomponentnumber, productiontrackingforproductdto.operation_id AS operation_id, productiontrackingforproductdto.product_id::integer AS product_id, productiontrackingforproductdto.productnumber AS productnumber, productiontrackingforproductdto.productunit AS productunit, productiontrackingforproductdto.plannedquantity AS plannedquantity, SUM(productiontrackingforproductdto.usedquantity) AS usedquantity FROM productioncounting_productiontrackingforproductdto productiontrackingforproductdto GROUP BY productiontrackingforproductdto.active, productiontrackingforproductdto.order_id, productiontrackingforproductdto.ordernumber, productiontrackingforproductdto.technologyoperationcomponent_id, productiontrackingforproductdto.technologyoperationcomponentnumber, productiontrackingforproductdto.operation_id, productiontrackingforproductdto.product_id, productiontrackingforproductdto.productnumber, productiontrackingforproductdto.productunit, productiontrackingforproductdto.plannedquantity;

-- end


-- #QCADOO-432

CREATE OR REPLACE FUNCTION generate_and_set_resource_number(_time timestamp) RETURNS text AS $$ DECLARE _pattern text; _year numeric;_sequence_name text; _sequence_value numeric; _tmp text; _seq text; _number text; BEGIN _pattern := '#year/#seq'; _year := EXTRACT(year FROM _time); _sequence_name := 'materialflowresources_resource_number_' || _year; SELECT sequence_name INTO _tmp FROM information_schema.sequences WHERE sequence_schema = 'public' AND sequence_name = _sequence_name;IF _tmp IS NULL THEN EXECUTE 'CREATE SEQUENCE ' || _sequence_name || ';'; END IF; SELECT nextval(_sequence_name) INTO _sequence_value;_seq := to_char(_sequence_value, 'fm00000'); IF _seq LIKE '%#%' THEN _seq  := _sequence_value; END IF; _number := _pattern;_number := REPLACE(_number, '#year', _year::text); _number := REPLACE(_number, '#seq', _seq); RETURN _number; END; $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION generate_and_set_resource_number_trigger() RETURNS trigger AS $$ BEGIN NEW.number := generate_and_set_resource_number(NEW.time); RETURN NEW; END; $$ LANGUAGE 'plpgsql';

CREATE TRIGGER materialflowresources_resource_trigger_number BEFORE INSERT ON materialflowresources_resource FOR EACH ROW EXECUTE PROCEDURE generate_and_set_resource_number_trigger();

-- end #QCADOO-432


-- end #QCADOO-433

CREATE OR REPLACE FUNCTION generate_document_number(_translated_type text) RETURNS text AS $$ DECLARE _pattern text; _sequence_name text; _sequence_value numeric; _tmp text; _seq text; _number text; BEGIN _pattern := '#translated_type/#seq'; _sequence_name := 'materialflowresources_document_number_' || LOWER(_translated_type); SELECT sequence_name INTO _tmp FROM information_schema.sequences WHERE sequence_schema = 'public' AND sequence_name = _sequence_name; IF _tmp IS NULL THEN EXECUTE 'CREATE SEQUENCE ' || _sequence_name || ';'; END IF; SELECT nextval(_sequence_name) INTO _sequence_value; _seq := to_char(_sequence_value, 'fm00000'); IF _seq LIKE '%#%' THEN _seq := _sequence_value; END IF; _number := _pattern; _number := REPLACE(_number, '#translated_type', _translated_type); _number := REPLACE(_number, '#seq', _seq); RETURN _number; END; $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION generate_and_set_document_number_trigger() RETURNS trigger AS $$ BEGIN NEW.number := generate_document_number(NEW.number); IF NEW.name IS NULL THEN NEW.name := NEW.number; END IF; RETURN NEW; END; $$ LANGUAGE 'plpgsql';

CREATE TRIGGER materialflowresources_document_trigger_number BEFORE INSERT ON materialflowresources_document FOR EACH ROW EXECUTE PROCEDURE generate_and_set_document_number_trigger();

-- end #QCADOO-433


-- #GOODFOOD-1196

CREATE SEQUENCE cmmsmachineparts_maintenanceevent_number_seq;

CREATE OR REPLACE FUNCTION generate_maintenanceevent_number() RETURNS text AS $$ DECLARE _pattern text; _sequence_name text; _sequence_value numeric; _tmp text; _seq text; _number text; BEGIN _pattern  := '#seq'; SELECT nextval('cmmsmachineparts_maintenanceevent_number_seq') INTO _sequence_value; _seq := to_char(_sequence_value, 'fm000000'); IF _seq LIKE '%#%' THEN _seq = _sequence_value; END IF; _number := _pattern; _number := REPLACE(_number, '#seq', _seq); RETURN _number; END; $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION generate_and_set_maintenanceevent_number_trigger() RETURNS trigger AS $$ BEGIN NEW.number := generate_maintenanceevent_number(); RETURN NEW; END; $$ LANGUAGE 'plpgsql';

CREATE TRIGGER cmmsmachineparts_maintenanceevent_trigger_number BEFORE INSERT ON cmmsmachineparts_maintenanceevent FOR EACH ROW EXECUTE PROCEDURE generate_and_set_maintenanceevent_number_trigger();

-- end #GOODFOOD-1196


-- VIEW: technologies_technologydto

DROP TABLE IF EXISTS technologies_technologydto;

CREATE OR REPLACE VIEW technologies_technologydto AS SELECT technology.id, technology.name, technology.number, technology.externalsynchronized, technology.master, technology.state, product.number AS productnumber, product.globaltypeofmaterial AS productglobaltypeofmaterial, tg.number AS technologygroupnumber, division.name AS divisionname, product.name AS productname, technology.technologytype, technology.active, technology.standardPerformanceTechnology, ''::text as generatorName FROM technologies_technology technology LEFT JOIN basic_product product ON technology.product_id = product.id LEFT JOIN basic_division division ON technology.division_id = division.id LEFT JOIN technologies_technologygroup tg ON technology.technologygroup_id = tg.id;


-- end


-- VIEW: orders_orderdto

DROP TABLE IF EXISTS orders_orderdto;

CREATE OR REPLACE VIEW orders_orderdto AS SELECT id, active, number, name, state, typeofproductionrecording FROM orders_order;

-- end


-- VIEW: materialflowresources_documentdto

DROP TABLE IF EXISTS materialflowresources_documentdto;

CREATE OR REPLACE VIEW materialflowresources_documentdto AS SELECT document.id AS id, document.number AS number, document.description AS description, document.name AS name, document.type AS type, document.time AS time, document.state AS state, document.active AS active, locationfrom.id::integer AS locationfrom_id, locationfrom.name AS locationfromname, locationto.id::integer AS locationto_id, locationto.name AS locationtoname, company.id::integer AS company_id, company.name AS companyname, securityuser.id::integer AS user_id, securityuser.firstname || ' ' || securityuser.lastname AS username, maintenanceevent.id::integer AS maintenanceevent_id, maintenanceevent.number AS maintenanceeventnumber, plannedevent.id::integer AS plannedevent_id, plannedevent.number AS plannedeventnumber, delivery.id::integer AS delivery_id, delivery.number AS deliverynumber, document.inBuffer FROM materialflowresources_document document LEFT JOIN materialflow_location locationfrom ON locationfrom.id = document.locationfrom_id LEFT JOIN materialflow_location locationto ON locationto.id = document.locationto_id LEFT JOIN basic_company company ON company.id = document.company_id LEFT JOIN qcadoosecurity_user securityuser ON securityuser.id = document.user_id LEFT JOIN cmmsmachineparts_maintenanceevent maintenanceevent ON maintenanceevent.id = document.maintenanceevent_id LEFT JOIN cmmsmachineparts_plannedevent plannedevent ON plannedevent.id = document.plannedevent_id LEFT JOIN deliveries_delivery delivery ON delivery.id = document.delivery_id;

-- end


-- VIEW: materialflowresource_resourcestock

DROP TABLE IF EXISTS materialflowresources_resourcestockdto;

CREATE OR REPLACE VIEW materialflowresources_orderedquantitystock AS SELECT COALESCE(SUM(orderedproduct.orderedquantity), 0::numeric) AS orderedquantity, resource.id AS resource_id FROM materialflowresources_resourcestock resource JOIN deliveries_orderedproduct orderedproduct ON (orderedproduct.product_id = resource.product_id) JOIN deliveries_delivery delivery ON (orderedproduct.delivery_id = delivery.id AND delivery.active = true AND delivery.location_id = resource.location_id AND (delivery.state::text = ANY (ARRAY['01draft'::character varying::text, '02prepared'::character varying::text, '03duringCorrection'::character varying::text, '05approved'::character varying::text]))) GROUP BY resource.id;

CREATE OR REPLACE VIEW materialflowresources_resourcestockdto_internal AS SELECT row_number() OVER () AS id, resource.location_id, resource.product_id::integer, resource.quantity AS quantity, COALESCE(orderedquantity.orderedquantity, 0::numeric) AS orderedquantity, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id) AS minimumstate, reservedQuantity, availableQuantity FROM materialflowresources_resourcestock resource LEFT JOIN materialflowresources_orderedquantitystock orderedquantity ON (orderedquantity.resource_id = resource.id) GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity, reservedQuantity, availableQuantity, quantity;

CREATE OR REPLACE VIEW materialflowresources_resourcestockdto AS SELECT internal.*, location.number AS locationNumber, location.name AS locationName, product.number AS productNumber, product.name AS productName, product.unit AS productUnit FROM materialflowresources_resourcestockdto_internal internal JOIN materialflow_location location ON (location.id = internal.location_id) JOIN basic_product product ON (product.id = internal.product_id);

-- end


-- VIEW: cmmsmachineparts_worktimeforuserdto

DROP TABLE IF EXISTS cmmsmachineparts_worktimeforuserdto;

CREATE OR REPLACE VIEW cmmsmachineparts_worktimeforuserdto_internal AS SELECT u.username AS username, swt.effectiveexecutiontimestart AS startdate, swt.effectiveexecutiontimeend AS finishdate, swt.labortime AS duration, me.number AS eventNumber, me.type AS eventtype, COALESCE(s.number, w.number, p.number, d.number, f.number) AS objectnumber, null AS actionname FROM cmmsmachineparts_staffworktime swt JOIN qcadoosecurity_user u ON swt.worker_id = u.staff_id JOIN cmmsmachineparts_maintenanceevent me ON me.id = swt.maintenanceevent_id JOIN basic_factory f ON me.factory_id = f.id JOIN basic_division d ON me.division_id = d.id LEFT JOIN productionlines_productionline p ON me.productionline_id = p.id LEFT JOIN basic_workstation w ON me.workstation_id = w.id LEFT JOIN basic_subassembly s ON me.subassembly_id = s.id union all SELECT u.username AS username, per.startdate AS startdate, per.finishdate AS finishdate, per.duration AS duration, pe.number AS eventnumber, pe.type AS eventtype, COALESCE(s.number, w.number, p.number, d.number, f.number) AS objectnumber, a.name AS actionname FROM cmmsmachineparts_plannedeventrealization per JOIN qcadoosecurity_user u ON per.worker_id = u.staff_id JOIN cmmsmachineparts_plannedevent pe ON pe.id = per.plannedevent_id JOIN basic_factory f ON pe.factory_id = f.id JOIN basic_division d ON pe.division_id = d.id LEFT JOIN productionlines_productionline p ON pe.productionline_id = p.id LEFT JOIN basic_workstation w ON pe.workstation_id = w.id LEFT JOIN basic_subassembly s ON pe.subassembly_id = s.id LEFT JOIN cmmsmachineparts_actionforplannedevent afpe ON per.action_id = afpe.id LEFT JOIN cmmsmachineparts_action a ON afpe.action_id = a.id;

CREATE OR REPLACE VIEW cmmsmachineparts_worktimeforuserdto AS SELECT row_number() OVER () AS id, internal.* FROM cmmsmachineparts_worktimeforuserdto_internal internal;

-- end


-- VIEW: storageLocationDto

DROP TABLE IF EXISTS materialflowresources_storagelocationdto;

CREATE OR REPLACE VIEW materialflowresources_storagelocationdto_internal AS SELECT location.number AS locationNumber, storageLocation.number AS storageLocationNumber, COALESCE(product.number, storageProduct.number) AS productNumber, COALESCE(product.name, storageProduct.name) AS productName, resourceCode.code AS additionalCode, COALESCE(SUM(resource.quantity), 0::numeric) AS resourceQuantity, COALESCE(product.unit, storageProduct.unit) AS productUnit, COALESCE(SUM(resource.quantityinadditionalunit), 0::numeric) AS quantityInAdditionalUnit, COALESCE(product.additionalunit, product.unit, storageProduct.additionalunit, storageProduct.unit) AS productAdditionalUnit FROM materialflowresources_storagelocation storageLocation JOIN materialflow_location location ON storageLocation.location_id = location.id LEFT JOIN materialflowresources_resource resource ON resource.storagelocation_id = storageLocation.id LEFT JOIN basic_product product ON product.id = resource.product_id LEFT JOIN basic_product storageProduct ON storageProduct.id = storageLocation.product_id LEFT JOIN basic_additionalcode resourceCode ON resourceCode.id = resource.additionalcode_id  where storageLocation.active = true GROUP BY locationNumber, storageLocationNumber, productNumber, productName, additionalCode, productUnit, productAdditionalUnit;

CREATE OR REPLACE VIEW materialflowresources_storagelocationdto AS SELECT row_number() OVER () AS id, internal.* FROM materialflowresources_storagelocationdto_internal internal;

-- end


-- VIEW: positionDto

DROP TABLE IF EXISTS materialflowresources_positiondto;

CREATE OR REPLACE VIEW materialflowresources_positiondto AS SELECT position.id AS id, locFrom.number AS locationFrom, locTo.number AS locationTo, product.number AS productNumber, product.name AS productName, position.quantity AS quantity, position.price AS price, product.unit AS productUnit, document.time AS documentDate, position.expirationdate::TIMESTAMP WITHOUT TIME ZONE AS expirationDate, position.productiondate::TIMESTAMP WITHOUT TIME ZONE AS productionDate, document.type AS documentType, document.state AS state, document.number AS documentNumber, document.name AS documentName, company.name AS companyName, (CASE WHEN address.name IS NULL THEN address.number ELSE address.number::text || ' - '::text || address.name::text END) AS documentAddress, position.batch AS batch, storageLoc.number AS storageLocation, position.waste AS waste, delivery.number AS deliveryNumber, plannedEvent.number AS plannedEventNumber, maintenanceEvent.number AS maintenanceEventNumber FROM materialflowresources_position position JOIN materialflowresources_document document ON position.document_id = document.id LEFT JOIN materialflow_location locFrom ON document.locationfrom_id = locFrom.id LEFT JOIN materialflow_location locTo ON document.locationto_id = locTo.id JOIN basic_product product ON position.product_id = product.id LEFT JOIN basic_company company ON document.company_id = company.id LEFT JOIN basic_address address ON document.address_id = address.id LEFT JOIN materialflowresources_storagelocation storageLoc ON position.storagelocation_id = storageLoc.id LEFT JOIN cmmsmachineparts_maintenanceevent maintenanceEvent ON document.maintenanceevent_id = maintenanceEvent.id LEFT JOIN cmmsmachineparts_plannedevent plannedEvent ON document.plannedevent_id = plannedEvent.id LEFT JOIN deliveries_delivery delivery ON document.delivery_id = delivery.id;

-- end


-- VIEW: masterorders_masterorderpositiondto

DROP TABLE IF EXISTS masterorders_masterorderpositiondto;

CREATE OR REPLACE VIEW masterorders_masterorderposition_oneproduct AS SELECT (SELECT COALESCE(max(masterorders_masterorderproduct.id), 0::integer) FROM masterorders_masterorderproduct) + row_number() OVER () AS id, masterorderdefinition.name, masterorder.id::integer AS masterorderid, masterorder.product_id::integer AS productid, masterorderproduct.id::integer AS masterorderproductid, masterorder.masterordertype, masterorder.name AS masterordername, masterorder.number, masterorder.deadline, masterorder.masterorderstate AS masterorderstatus, masterorder.masterorderpositionstatus, COALESCE(masterorder.masterorderquantity, 0::numeric) AS masterorderquantity, COALESCE((SELECT SUM(orders.plannedquantity)), 0::numeric) AS cumulatedmasterorderquantity, COALESCE((SELECT SUM(orders.donequantity)), 0::numeric) AS producedorderquantity, CASE WHEN (COALESCE(masterorderproduct.masterorderquantity, 0::numeric) - COALESCE((SELECT SUM(orders.donequantity)), 0::numeric)) > 0 THEN (COALESCE(masterorderproduct.masterorderquantity, 0::numeric) - COALESCE((SELECT SUM(orders.donequantity)), 0::numeric)) ELSE 0::numeric END AS lefttorelease, masterorder.comments, product.number AS productnumber, product.name AS productname, product.unit, technology.number AS technologyname, company.name AS companyname, masterorder.active FROM masterorders_masterorder masterorder LEFT JOIN masterorders_masterorderdefinition masterorderdefinition ON masterorderdefinition.id = masterorder.masterorderdefinition_id LEFT JOIN masterorders_masterorderproduct masterorderproduct ON masterorderproduct.masterorder_id = masterorder.id LEFT JOIN basic_product product ON product.id = masterorder.product_id LEFT JOIN basic_company company ON company.id = masterorder.company_id LEFT JOIN technologies_technology technology ON technology.id = masterorder.technology_id LEFT JOIN orders_order orders ON orders.masterorder_id = masterorder.id AND orders.product_id = masterorder.product_id WHERE masterorder.masterordertype::text = '02oneProduct'::text GROUP BY masterorderdefinition.name, masterorder.id, masterorder.product_id, masterorderproduct.id, masterorder.masterordertype, masterorder.name, masterorder.deadline, masterorder.masterorderstate, masterorder.masterorderpositionstatus, masterorder.comments, product.number, product.name, product.unit, technology.number, company.name, masterorder.active;

CREATE OR REPLACE VIEW masterorders_masterorderposition_manyproducts AS SELECT COALESCE(masterorderproduct.id, 0::integer), masterorderdefinition.name, masterorder.id::integer AS masterorderid, masterorderproduct.product_id::integer AS productid, masterorderproduct.id::integer AS masterorderproductid, masterorder.masterordertype, masterorder.name AS masterordername, masterorder.number, masterorder.deadline, masterorder.masterorderstate AS masterorderstatus, masterorderproduct.masterorderpositionstatus, COALESCE(masterorderproduct.masterorderquantity, 0::numeric) AS masterorderquantity, COALESCE((SELECT SUM(orders.plannedquantity)), 0::numeric) AS cumulatedmasterorderquantity, COALESCE((SELECT SUM(orders.donequantity)), 0::numeric) AS producedorderquantity, CASE WHEN (COALESCE(masterorderproduct.masterorderquantity, 0::numeric) - COALESCE((SELECT SUM(orders.donequantity)), 0::numeric)) > 0 THEN (COALESCE(masterorderproduct.masterorderquantity, 0::numeric)- COALESCE((SELECT SUM(orders.donequantity)), 0::numeric)) ELSE 0::numeric END AS lefttorelease, masterorderproduct.comments, product.number AS productnumber, product.name AS productname, product.unit, technology.number AS technologyname, company.name AS companyname, masterorder.active FROM masterorders_masterorder masterorder LEFT JOIN masterorders_masterorderdefinition masterorderdefinition ON masterorderdefinition.id = masterorder.masterorderdefinition_id LEFT JOIN masterorders_masterorderproduct masterorderproduct ON masterorderproduct.masterorder_id = masterorder.id LEFT JOIN basic_product product ON product.id = masterorderproduct.product_id LEFT JOIN basic_company company ON company.id = masterorder.company_id LEFT JOIN technologies_technology technology ON technology.id = masterorderproduct.technology_id LEFT JOIN orders_order orders ON orders.masterorderproductcomponent_id = masterorderproduct.id AND orders.masterorder_id = masterorderproduct.masterorder_id AND orders.product_id = masterorderproduct.product_id WHERE masterorder.masterordertype = '03manyProducts' AND masterorderproduct.id IS NOT NULL GROUP BY masterorderdefinition.name, masterorder.id, masterorder.product_id, masterorderproduct.id, masterorder.masterordertype, masterorder.name, masterorder.deadline, masterorder.masterorderstate, masterorder.masterorderpositionstatus, masterorder.comments, product.number, product.name, product.unit, technology.number, company.name, masterorder.active;

CREATE OR REPLACE VIEW masterorders_masterorderpositiondto AS SELECT * FROM masterorders_masterorderposition_oneproduct UNION ALL SELECT * FROM masterorders_masterorderposition_manyproducts;

-- end

-- production tracking number sequence

CREATE SEQUENCE productioncounting_productiontracking_number_seq;

CREATE OR REPLACE FUNCTION generate_productiontracking_number() RETURNS text AS $$ DECLARE _pattern text; _sequence_name text; _sequence_value numeric; _tmp text; _seq text; _number text; BEGIN _pattern := '#seq'; select nextval('productioncounting_productiontracking_number_seq') into _sequence_value; _seq := to_char(_sequence_value, 'fm000000'); if _seq like '%#%' then _seq := _sequence_value; end if; _number := _pattern; _number := replace(_number, '#seq', _seq); RETURN _number; END; $$ LANGUAGE 'plpgsql';

-- end
