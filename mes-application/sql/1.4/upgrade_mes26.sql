-- Actions and planned events
-- last touched 19.08.2015 by kama
CREATE TABLE cmmsmachineparts_action
(
  id bigint NOT NULL,
  name character varying(255),
  appliesto character varying(255) DEFAULT '01workstationOrSubassembly'::character varying,
  CONSTRAINT cmmsmachineparts_action_pkey PRIMARY KEY (id)
);

CREATE TABLE cmmsmachineparts_plannedeventcontext
(
  id bigint NOT NULL,
  factory_id bigint,
  division_id bigint,
  generated boolean DEFAULT false,
  confirmed boolean DEFAULT false,
  CONSTRAINT cmmsmachineparts_plannedeventcontext_pkey PRIMARY KEY (id),
  CONSTRAINT plannedeventcontext_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE,
  CONSTRAINT plannedeventcontext_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE
);

CREATE TABLE cmmsmachineparts_plannedevent
(
  id bigint NOT NULL,
  "number" character varying(255),
  type character varying(255) DEFAULT '01review'::character varying,
  description character varying(255),
  owner_id bigint,
  ownername character varying(255),
  state character varying(255) DEFAULT '01new'::character varying,
  factory_id bigint,
  division_id bigint,
  productionline_id bigint,
  workstation_id bigint,
  subassembly_id bigint,
  plannedeventcontext_id bigint,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  plannedseparately boolean DEFAULT false,
  afterreview boolean DEFAULT false,
  requiresshutdown boolean DEFAULT false,
  company_id bigint,
  basedon character varying(255) DEFAULT '01date'::character varying,
  "date" date,
  counter numeric(14,5),
  countertolerance numeric(14,5),
  duration integer DEFAULT 0,
  effectiveduration integer DEFAULT 0,
  effectivecounter numeric(14,5),
  startdate timestamp without time zone,
  finishdate timestamp without time zone,
  isdeadline boolean,
  solutiondescription text,
  CONSTRAINT cmmsmachineparts_plannedevent_pkey PRIMARY KEY (id),
  CONSTRAINT plannedevent_staff_fkey FOREIGN KEY (owner_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT plannedevent_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT plannedevent_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT plannedevent_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE,
  CONSTRAINT plannedevent_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE,
  CONSTRAINT plannedevent_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE,
  CONSTRAINT plannedevent_subassembly_fkey FOREIGN KEY (subassembly_id)
      REFERENCES basic_subassembly (id) DEFERRABLE,
  CONSTRAINT plannedevent_plannedeventcontext_fkey FOREIGN KEY (plannedeventcontext_id)
      REFERENCES cmmsmachineparts_plannedeventcontext (id) DEFERRABLE
);

CREATE TABLE cmmsmachineparts_actionforplannedevent
(
  id bigint NOT NULL,
  plannedevent_id bigint,
  description character varying(1024),
  action_id bigint,
  responsibleworker_id bigint,
  responsibleworkername character varying(255),
  state character varying(255) DEFAULT '01correct'::character varying,
  reason character varying(1024),
  CONSTRAINT cmmsmachineparts_actionforplannedevent_pkey PRIMARY KEY (id),
  CONSTRAINT actionforplannedevet_plannedevent_fkey FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE,
  CONSTRAINT actionforplannedevet_action_fkey FOREIGN KEY (action_id)
      REFERENCES cmmsmachineparts_action (id) DEFERRABLE,
  CONSTRAINT actionforplannedevet_staff_fkey FOREIGN KEY (responsibleworker_id)
      REFERENCES basic_staff (id) DEFERRABLE
);

ALTER TABLE cmmsmachineparts_machinepartforevent ADD COLUMN plannedevent_id bigint;
ALTER TABLE cmmsmachineparts_machinepartforevent
  ADD CONSTRAINT machinepartforevent_plannedevent_fkey FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE;

ALTER TABLE cmmsmachineparts_machinepartforevent ADD COLUMN machinepartname character varying(255);
ALTER TABLE cmmsmachineparts_machinepartforevent ADD COLUMN machinepartnumber character varying(255);
ALTER TABLE cmmsmachineparts_machinepartforevent ADD COLUMN machinepartunit character varying(255);
ALTER TABLE cmmsmachineparts_machinepartforevent ADD COLUMN warehousenumber character varying(255);

CREATE TABLE jointable_action_subassembly
(
  action_id bigint NOT NULL,
  subassembly_id bigint NOT NULL,
  CONSTRAINT jointable_action_subassembly_pkey PRIMARY KEY (subassembly_id, action_id),
  CONSTRAINT action_subassembly_action_fkey FOREIGN KEY (action_id)
      REFERENCES cmmsmachineparts_action (id) DEFERRABLE,
  CONSTRAINT action_subassembly_subassembly_fkey FOREIGN KEY (subassembly_id)
      REFERENCES basic_subassembly (id) DEFERRABLE
);

CREATE TABLE jointable_action_workstation
(
  action_id bigint NOT NULL,
  workstation_id bigint NOT NULL,
  CONSTRAINT jointable_action_workstation_pkey PRIMARY KEY (workstation_id, action_id),
  CONSTRAINT action_workstation_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE,
  CONSTRAINT action_workstation_action_fkey FOREIGN KEY (action_id)
      REFERENCES cmmsmachineparts_action (id) DEFERRABLE
);

CREATE TABLE jointable_action_workstationtype
(
  action_id bigint NOT NULL,
  workstationtype_id bigint NOT NULL,
  CONSTRAINT jointable_action_workstationtype_pkey PRIMARY KEY (workstationtype_id, action_id),
  CONSTRAINT action_workstationtype_workstationtype_fkey FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) DEFERRABLE,
  CONSTRAINT action_workstationtype_action_fkey FOREIGN KEY (action_id)
      REFERENCES cmmsmachineparts_action (id) DEFERRABLE
);
-- end

-- Added event realization
-- last touched 10.08.2015 by pako

CREATE TABLE cmmsmachineparts_plannedeventrealization
(
  id bigint NOT NULL,
  plannedevent_id bigint,
  worker_id bigint,
  action_id bigint,
  startdate timestamp without time zone,
  finishdate timestamp without time zone,
  duration integer DEFAULT 0,
  CONSTRAINT cmmsmachineparts_plannedeventrealization_pkey PRIMARY KEY (id),
  CONSTRAINT plannedeventrealization_staff_fkey FOREIGN KEY (worker_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT plannedeventrealization_planned_event FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE,
  CONSTRAINT plannedeventrealization_action FOREIGN KEY (action_id)
      REFERENCES cmmsmachineparts_action (id) DEFERRABLE
);

-- end

-- Added event attachment
-- last touched 11.08.2015 by pako

CREATE TABLE cmmsmachineparts_plannedeventattachment
(
  id bigint NOT NULL,
  plannedevent_id bigint,
  attachment character varying(255),
  name character varying(255),
  size numeric(12,5),
  ext character varying(255),
  CONSTRAINT cmmsmachineparts_plannedeventattachment_pkey PRIMARY KEY (id),
  CONSTRAINT plannedeventattachment_plannedevent FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE
);

-- order plannings optimizations
CREATE SEQUENCE orders_orderplanninglistdto_id_seq;
create or replace view orders_orderPlanningListDto as
select
	o.id, o.active, o.number, o.name, o.dateFrom, o.dateTo, o.startDate, o.finishDate, o.state, o.externalNumber, o.externalSynchronized, o.isSubcontracted, o.plannedQuantity, o.workPlanDelivered,
	product.number as productNumber, tech.number as technologyNumber, product.unit, line.number as productionLineNumber, master.number as masterOrderNumber, division.name as divisionName
	from orders_order o
	join basic_product product on (o.product_id = product.id)
	left join technologies_technology tech on (o.technology_id = tech.id)
	join productionLines_productionLine line on (o.productionline_id = line.id)
	left join masterOrders_masterOrder  master on (o.masterorder_id = master.id)
	left join basic_division division on (tech.division_id = division.id);
-- end
--
-- versionable
CREATE OR REPLACE FUNCTION update_version() RETURNS VOID AS $$ DECLARE row record;  BEGIN FOR row IN SELECT tablename FROM pg_tables p INNER  JOIN information_schema.columns c on p.tablename = c.table_name WHERE c.table_schema = 'public' and p.schemaname = 'public' and c.column_name = 'id' and data_type = 'bigint'  LOOP  EXECUTE 'ALTER TABLE ' || quote_ident(row.tablename) || ' ADD entityVersion BIGINT DEFAULT 0;';  END LOOP;  END;  $$ LANGUAGE 'plpgsql';
SELECT * FROM update_version();
DROP FUNCTION update_version();
--end

-- QCADOOCLS-4373
alter table qcadooplugin_plugin add groupname character varying(255);
update qcadooplugin_plugin set version = '1.3.0';
-- end

-- materialflowresources_document
-- last touched 25.08.2015 by kasi
ALTER TABLE materialflowresources_document ADD COLUMN plannedevent_id bigint;
ALTER TABLE materialflowresources_document
  ADD CONSTRAINT plannedevent_fkey FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE;

-- end

-- Planned event state change
-- last touched 25.08.2015 by pako

CREATE TABLE cmmsmachineparts_plannedeventstatechange
(
  id bigint NOT NULL,
  dateandtime timestamp without time zone,
  sourcestate character varying(255),
  targetstate character varying(255),
  status character varying(255),
  phase integer,
  worker character varying(255),
  plannedevent_id bigint,
  shift_id bigint,
  comment character varying(255),
  commentrequired boolean DEFAULT false,
  CONSTRAINT cmmsmachineparts_plannedeventstatechange_pkey PRIMARY KEY (id),
  CONSTRAINT plannedeventstatechange_plannedevent_fkey FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE,
  CONSTRAINT plannedeventstatechange_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE
);

ALTER TABLE states_message ADD COLUMN plannedeventstatechange_id bigint;
ALTER TABLE states_message
  ADD CONSTRAINT message_plannedeventstatechange_fkey FOREIGN KEY (plannedeventstatechange_id)
      REFERENCES cmmsmachineparts_plannedeventstatechange (id) DEFERRABLE;
-- end

-- Changing events to planned, related planned events
-- last touched 26.08.2015 by kama
ALTER TABLE cmmsmachineparts_maintenanceeventstatechange ADD COLUMN plannedeventtype character varying(255);
ALTER TABLE cmmsmachineparts_maintenanceeventstatechange ADD COLUMN plannedeventtyperequired boolean;
ALTER TABLE cmmsmachineparts_maintenanceeventstatechange ALTER COLUMN plannedeventtyperequired SET DEFAULT false;
ALTER TABLE cmmsmachineparts_plannedevent ADD COLUMN maintenanceevent_id bigint;
ALTER TABLE cmmsmachineparts_plannedevent
  ADD CONSTRAINT plannedevent_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE;

CREATE TABLE jointable_plannedevent_plannedevent
(
  plannedevent_id bigint NOT NULL,
  relatedevent_id bigint NOT NULL,
  CONSTRAINT jointable_plannedevent_plannedevent_pkey PRIMARY KEY (plannedevent_id, relatedevent_id),
  CONSTRAINT plannedevent_relatedevent_fkey FOREIGN KEY (relatedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE,
  CONSTRAINT plannedevent_plannedevent_fkey FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE
);

-- end

-- Added missing scripts
-- last touched 01.09.2015 by kama

ALTER TABLE basic_factory ADD COLUMN warehouse_id bigint;
ALTER TABLE basic_factory
  ADD CONSTRAINT factory_warehouse_fkey FOREIGN KEY (warehouse_id)
      REFERENCES materialflow_location (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE basic_parameter ADD COLUMN labelsbtpath character varying(255);

CREATE TABLE jointable_plannedevent_staff
(
  plannedevent_id bigint NOT NULL,
  staff_id bigint NOT NULL,
  CONSTRAINT jointable_plannedevent_staff_pkey PRIMARY KEY (staff_id, plannedevent_id),
  CONSTRAINT plannedevent_staff_skey FOREIGN KEY (staff_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT plannedevent_plannedevent_fkey FOREIGN KEY (plannedevent_id)
      REFERENCES cmmsmachineparts_plannedevent (id) DEFERRABLE
);


-- end