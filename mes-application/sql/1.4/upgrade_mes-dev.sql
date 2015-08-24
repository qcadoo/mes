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
)

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
)

-- order plannings optimizations
drop table orders_orderPlanningListDto;
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
	left join basic_division division on (tech.division_id = division.id)
-- end
