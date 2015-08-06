-- Added machine parts for event
-- last touched 23.07.2015 by kama

CREATE TABLE cmmsmachineparts_machinepartforevent
(
  id bigint NOT NULL,
  maintenanceevent_id bigint,
  machinepart_id bigint,
  warehouse_id bigint,
  plannedquantity numeric(14,5),
  availablequantity numeric(14,5),
  CONSTRAINT cmmsmachineparts_machinepartforevent_pkey PRIMARY KEY (id),
  CONSTRAINT machinepartforevent_warehouse_fkey FOREIGN KEY (warehouse_id)
      REFERENCES materialflow_location (id) DEFERRABLE,
  CONSTRAINT machinepartforevent_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE,
  CONSTRAINT machinepartforevent_machinepart_fkey FOREIGN KEY (machinepart_id)
      REFERENCES basic_product (id) DEFERRABLE
);
-- GOODFOOD-614

CREATE TABLE cmmsmachineparts_maintenanceeventcontext
(
  id bigint NOT NULL,
  factory_id bigint,
  division_id bigint,
  generated boolean DEFAULT false,
  confirmed boolean DEFAULT false,
  CONSTRAINT cmmsmachineparts_maintenanceeventcontext_pkey PRIMARY KEY (id),
  CONSTRAINT maintenanceeventcontext_division_fkey FOREIGN KEY (division_id) REFERENCES basic_division (id) DEFERRABLE,
  CONSTRAINT maintenanceeventcontext_factory_fkey FOREIGN KEY (factory_id) REFERENCES basic_factory (id) DEFERRABLE
);

ALTER TABLE cmmsmachineparts_maintenanceevent ADD maintenanceeventcontext_id bigint;
ALTER TABLE cmmsmachineparts_maintenanceevent ADD CONSTRAINT maintenanceevent_maintenanceeventcontext_fkey FOREIGN KEY (maintenanceeventcontext_id)
      REFERENCES cmmsmachineparts_maintenanceeventcontext (id) DEFERRABLE;

-- end


-- Table: assignmenttoshift_assignmenttoshift
-- last touched 05.08.2015 by lupo

ALTER TABLE assignmenttoshift_assignmenttoshift ADD COLUMN factory_id bigint;

ALTER TABLE assignmenttoshift_assignmenttoshift
  ADD CONSTRAINT assignmenttoshift_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE;
      
-- end


-- Table: assignmenttoshift_assignmenttoshiftreport
-- last touched 05.08.2015 by lupo

ALTER TABLE assignmenttoshift_assignmenttoshiftreport ADD COLUMN factory_id bigint;

ALTER TABLE assignmenttoshift_assignmenttoshiftreport
  ADD CONSTRAINT assignmenttoshiftreport_factory_fkey FOREIGN KEY (factory_id)
      REFERENCES basic_factory (id) DEFERRABLE;

-- end


-- Table: cmmsmachineparts_maintenanceeventstatechange
-- last touched 06.08.2015 by lupo

ALTER TABLE cmmsmachineparts_maintenanceeventstatechange ADD COLUMN comment character varying(255);

ALTER TABLE cmmsmachineparts_maintenanceeventstatechange ADD COLUMN commentrequired boolean;
ALTER TABLE cmmsmachineparts_maintenanceeventstatechange ALTER COLUMN commentrequired SET DEFAULT false;

-- end


-- Table: materialflowresources_document
-- last touched 06.08.2015 by lupo

ALTER TABLE materialflowresources_document ADD COLUMN maintenanceevent_id bigint;

ALTER TABLE materialflowresources_document
  ADD CONSTRAINT document_maintenanceevent_fkey FOREIGN KEY (maintenanceevent_id)
      REFERENCES cmmsmachineparts_maintenanceevent (id) DEFERRABLE;
      
-- end


-- Table: productionlines_productionline
-- last touched 06.08.2015 by lupo

ALTER TABLE productionlines_productionline ADD COLUMN production boolean;
ALTER TABLE productionlines_productionline ALTER COLUMN production SET DEFAULT false;

-- end
