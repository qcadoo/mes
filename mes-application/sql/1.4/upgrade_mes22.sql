-- Added maintenance events
-- last touched 30.06.2015 by kama
CREATE TABLE cmmsmachineparts_maintenanceevent
(
  id bigint NOT NULL,
  "number" character varying(255),
  type character varying(255) DEFAULT '01failure'::character varying,
  description character varying(255),
  personreceiving_id bigint,
  personreceivingname character varying(255),
  state character varying(255) DEFAULT '01new'::character varying,
  createdate timestamp without time zone,
  updatedate timestamp without time zone,
  createuser character varying(255),
  updateuser character varying(255),
  factory_id bigint,
  division_id bigint,
  productionline_id bigint,
  workstation_id bigint,
  subassembly_id bigint,
  faulttype_id bigint,
  CONSTRAINT cmmsmachineparts_maintenanceevent_pkey PRIMARY KEY (id),
  CONSTRAINT maintenanceevent_faulttype_fkey FOREIGN KEY (faulttype_id)
      REFERENCES cmmsmachineparts_faulttype (id) DEFERRABLE,
  CONSTRAINT maintenanceevent_productionline_fkey FOREIGN KEY (productionline_id)
      REFERENCES productionlines_productionline (id) DEFERRABLE,
  CONSTRAINT maintenanceevent_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE,
  CONSTRAINT maintenanceevent_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE,
  CONSTRAINT maintenanceevent_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE,
  CONSTRAINT maintenanceevent_subassembly_fkey FOREIGN KEY (subassembly_id)
      REFERENCES basic_subassembly (id) DEFERRABLE,
  CONSTRAINT maintenanceevent_personreceiving_fkey FOREIGN KEY (personreceiving_id)
      REFERENCES basic_staff (id) DEFERRABLE
);

ALTER TABLE productionlines_factorystructureelement ADD COLUMN entityid integer;
ALTER TABLE productionlines_factorystructureelement ADD COLUMN maintenanceevent_id bigint;
ALTER TABLE productionlines_factorystructureelement
  ADD CONSTRAINT factorystructureelement_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE;

-- end

-- Added event attachments
-- last touched 07.07.2015 by kama

CREATE TABLE cmmsmachineparts_eventattachment
(
  id bigint NOT NULL,
  maintenanceevent_id bigint,
  attachment character varying(255),
  name character varying(255),
  size numeric(12,5),
  ext character varying(255),
  CONSTRAINT cmmsmachineparts_eventattachment_pkey PRIMARY KEY (id),
  CONSTRAINT eventattachment_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE
);

-- end

-- Added event state changes
-- last touched 07.07.2015 by kama

CREATE TABLE cmmsmachineparts_maintenanceeventstatechange
(
  id bigint NOT NULL,
  dateandtime timestamp without time zone,
  sourcestate character varying(255),
  targetstate character varying(255),
  status character varying(255),
  phase integer,
  worker character varying(255),
  maintenanceevent_id bigint,
  shift_id bigint,
  CONSTRAINT cmmsmachineparts_maintenanceeventstatechange_pkey PRIMARY KEY (id),
  CONSTRAINT maintenanceeventstatechange_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE,
  CONSTRAINT maintenanceeventstatechange_shift_fkey FOREIGN KEY (shift_id)
      REFERENCES basic_shift (id) DEFERRABLE
);


ALTER TABLE states_message ADD COLUMN maintenanceeventstatechange_id bigint;
ALTER TABLE states_message
  ADD CONSTRAINT message_maintenanceeventstatechange_fkey FOREIGN KEY (maintenanceeventstatechange_id)
      REFERENCES cmmsmachineparts_maintenanceeventstatechange (id) DEFERRABLE;
-- end


-- Added cmmsmachineparts_staffworktime table
-- last touched 07.07.2015 by kasi

CREATE TABLE cmmsmachineparts_staffworktime
(
  id bigint NOT NULL,
  maintenanceevent_id bigint,
  worker_id bigint,
  labortime integer DEFAULT 0,
  effectiveexecutiontimestart timestamp without time zone,
  effectiveexecutiontimeend timestamp without time zone,
  CONSTRAINT cmmsmachineparts_staffworktime_pkey PRIMARY KEY (id),
  CONSTRAINT staffworktime_worker_fkey FOREIGN KEY (worker_id)
      REFERENCES basic_staff (id) DEFERRABLE,
  CONSTRAINT worker_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE
);

--end


-- GOODFOOD-508
CREATE TABLE basic_subassemblytoworkstationhelper
(
  id bigint NOT NULL,
  subassembly_id bigint,
  type character varying(255),
  workstation_id bigint,

  CONSTRAINT basic_subassemblytoworkstationhelper_pkey PRIMARY KEY (id),
  CONSTRAINT subassemblytoworkstationhelper_workstation_fkey FOREIGN KEY (workstation_id)
    REFERENCES basic_workstation (id) DEFERRABLE,
  CONSTRAINT subassemblytoworkstationhelper_subassembly_fkey FOREIGN KEY (subassembly_id)
    REFERENCES basic_subassembly (id) DEFERRABLE
);

--end
-- Added solution description to maintenance event
-- last touched 7.07.2015 by pako

ALTER TABLE cmmsmachineparts_maintenanceevent ADD COLUMN solutiondescription text;

-- end

-- Added possible work time deviation to parameter
-- last touched 21.07.2015 by kama

ALTER TABLE basic_parameter ADD COLUMN possibleworktimedeviation numeric(12,5);
	
-- end

-- Missing column
-- last touched 21.07.2015 by pako

alter TABLE basic_subassembly add type character varying(255);

-- end