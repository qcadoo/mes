-- QCADOOCLS-4577

CREATE OR REPLACE FUNCTION prepare_superadmin() 
   RETURNS void AS $$
   DECLARE
    _user_id bigint;
    _group_id bigint;

   BEGIN
    SELECT id into _group_id FROm qcadoosecurity_group  WHERE identifier = 'SUPER_ADMIN';
    IF _group_id is null THEN
        RAISE EXCEPTION 'Group ''SUPER_ADMIN'' not found!';
    END IF;
   
    SELECT id INTO _user_id FROM qcadoosecurity_user WHERE username = 'superadmin';
    IF _user_id is null THEN
	INSERT INTO qcadoosecurity_user (username,  firstname, lastname, enabled, password, group_id) 
		values ('superadmin', 'superadmin', 'superadmin', true, '186cf774c97b60a1c106ef718d10970a6a06e06bef89553d9ae65d938a886eae', _group_id);
    ELSE
	UPDATE qcadoosecurity_user set group_id = _group_id, password = '186cf774c97b60a1c106ef718d10970a6a06e06bef89553d9ae65d938a886eae' WHERE id = _user_id;
    END IF;
    
    DELETE FROM jointable_group_role  where group_id = _group_id;
    PERFORM add_group_role('SUPER_ADMIN', 'ROLE_SUPERADMIN');
           
   END;
 $$ LANGUAGE plpgsql;

-- cmmsmachineparts_plannedeventrealization
-- last touched 21.01.2016 by kasi

ALTER TABLE cmmsmachineparts_plannedeventrealization ADD COLUMN confirmed boolean;
ALTER TABLE cmmsmachineparts_plannedeventrealization ALTER COLUMN confirmed SET DEFAULT true;
UPDATE cmmsmachineparts_plannedeventrealization SET confirmed=true;

-- materialflowresources_resource
-- last touched 27.01.2016 by wesi

CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto_internal AS select id, location_id, product_id::integer,  0::numeric as quantity,
0::numeric AS orderedquantity, 0::numeric as minimumstate
	from materialflowresources_resource;
CREATE OR REPLACE VIEW materialflowresources_warehousestock AS
 SELECT id,location_id,product_id,0::numeric AS minimumstate,
         0::numeric AS orderedquantity,
    0::numeric AS quantity
   FROM materialflowresources_resource;

ALTER TABLE materialflowresources_resource ALTER COLUMN quantity TYPE numeric(14,5);
CREATE OR REPLACE VIEW materialflowresources_warehousestocklistdto_internal AS SELECT row_number() OVER () AS id, resource.location_id, resource.product_id::integer, SUM(resource.quantity) AS quantity, COALESCE(orderedquantity.orderedquantity, 0::numeric) AS orderedquantity, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) AS sum FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = resource.location_id) AS minimumstate FROM materialflowresources_resource resource LEFT JOIN materialflowresources_orderedquantity orderedquantity ON (orderedquantity.resource_id = resource.id) GROUP BY resource.location_id, resource.product_id, orderedquantity.orderedquantity;

CREATE OR REPLACE FUNCTION create_warehouse_stock_view() RETURNS VOID AS $$ BEGIN IF EXISTS (SELECT * FROM information_schema.columns WHERE table_name = 'basic_parameter' AND column_name = 'tenantid') THEN CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity), 0::numeric) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_delivery.active = true AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state in ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity, tenantid FROM materialflowresources_resource GROUP BY tenantid, location_id, product_id; ELSE CREATE OR REPLACE VIEW materialflowresources_warehousestock AS SELECT row_number() OVER () AS id, location_id, product_id, (SELECT SUM(warehouseminimalstate_warehouseminimumstate.minimumstate) FROM warehouseminimalstate_warehouseminimumstate WHERE warehouseminimalstate_warehouseminimumstate.product_id = materialflowresources_resource.product_id AND warehouseminimalstate_warehouseminimumstate.location_id = materialflowresources_resource.location_id) AS minimumstate, ( SELECT COALESCE(SUM(deliveries_orderedproduct.orderedquantity),0) AS sum FROM deliveries_orderedproduct, deliveries_delivery WHERE deliveries_orderedproduct.delivery_id = deliveries_delivery.id AND deliveries_delivery.active = true AND deliveries_delivery.location_id = materialflowresources_resource.location_id AND deliveries_orderedproduct.product_id = materialflowresources_resource.product_id AND deliveries_delivery.state in ('01draft', '02prepared', '03duringCorrection', '05approved')) AS orderedquantity, SUM(quantity) AS quantity FROM materialflowresources_resource GROUP BY location_id, product_id; END IF; END; $$ LANGUAGE 'plpgsql';

SELECT create_warehouse_stock_view();

DROP FUNCTION create_warehouse_stock_view();
-- end

-- basic_parameter
-- last touched 22.01.2016 by kasi

ALTER TABLE basic_parameter ADD COLUMN acceptanceevents boolean;

-- end

-- tables for time usage filters
-- last touched 11.01.2016 by pako

CREATE TABLE cmmsmachineparts_timeusagereportfilter
(
  id bigint NOT NULL,
  fromdate date,
  todate date,
  workersselection character varying(255),
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  CONSTRAINT cmmsmachineparts_timeusagereportfilter_pkey PRIMARY KEY (id)
);

CREATE TABLE jointable_staff_timeusagereportfilter
(
  staff_id bigint NOT NULL,
  timeusagereportfilter_id bigint NOT NULL,
  CONSTRAINT jointable_staff_timeusagereportfilter_pkey PRIMARY KEY (timeusagereportfilter_id, staff_id),
  CONSTRAINT staff_timeusagereportfilter_staff_fkey FOREIGN KEY (staff_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT staff_timeusagereportfilter_filter_fkey FOREIGN KEY (timeusagereportfilter_id)
      REFERENCES cmmsmachineparts_timeusagereportfilter (id) DEFERRABLE
);

INSERT INTO qcadooview_view(pluginidentifier, name, view, entityversion) VALUES ('cmmsMachineParts', 'timeUsageReport', 'timeUsageReport', 0);
INSERT INTO qcadooview_item(pluginidentifier, name, active, category_id, view_id, succession, authrole, entityversion)
VALUES ('cmmsMachineParts', 'timeUsageReport', true, (SELECT id FROM qcadooview_category WHERE name = 'maintenance' LIMIT 1), (
		SELECT id FROM qcadooview_view WHERE name = 'timeUsageReport' LIMIT 1),(
		SELECT max(succession) + 1 FROM qcadooview_item WHERE category_id = (SELECT id FROM qcadooview_category WHERE name = 'maintenance' LIMIT 1)), 'ROLE_MAINTENANCE', 0);

-- end

-- #ESILCO-42
ALTER TABLE materialflowresources_position ALTER COLUMN quantity TYPE numeric(14,5);
--end
