-- Table: qcadoomodel_dictionary
-- changed: 18.10.2013

INSERT INTO qcadoomodel_dictionary(id, name, pluginidentifier, active)
    VALUES (nextval('hibernate_sequence') + 1000, 'paymentForm', 'deliveries', true);

-- end


-- Table: deliveries_delivery
-- changed: 18.10.2013

ALTER TABLE deliveries_delivery DROP COLUMN paymentForm;

-- end


-- Table: productioncounting_productionrecord
-- changed: 30.10.2013 [maku]

ALTER TABLE productioncounting_productionrecord ADD COLUMN laststatechangefails boolean DEFAULT false;
ALTER TABLE productioncounting_productionrecord ADD COLUMN laststatechangefailcause character varying(255);
ALTER TABLE productioncounting_productionrecord ADD COLUMN isexternalsynchronized boolean DEFAULT true;

-- end


-- Table: basic_company
-- changed: 28.11.2013

ALTER TABLE basic_company ADD COLUMN paymentform character varying(255);

-- end


-- Table: basic_division
-- changed: 21.11.2013

ALTER TABLE basic_division ADD COLUMN componentslocation_id bigint;
ALTER TABLE basic_division
  ADD CONSTRAINT division_componentslocation_fkey FOREIGN KEY (componentslocation_id)
      REFERENCES materialflow_location (id);
      
ALTER TABLE basic_division ADD COLUMN componentsoutputlocation_id bigint;
ALTER TABLE basic_division
  ADD CONSTRAINT division_componentsoutputlocation_fkey FOREIGN KEY (componentsoutputlocation_id)
      REFERENCES materialflow_location (id);

ALTER TABLE basic_division ADD COLUMN productsinputlocation_id bigint;
ALTER TABLE basic_division
  ADD CONSTRAINT division_productsinputlocation_fkey FOREIGN KEY (productsinputlocation_id)
      REFERENCES materialflow_location (id);

-- end


-- Table: basic_workstation
-- changed: 22.11.2013
      
CREATE TABLE basic_workstation
(
  id bigint NOT NULL,
  "number" character varying(255),
  name character varying(1024),
  description character varying(2048),
  workstationtype_id bigint,
  division_id bigint,
  operation_id bigint,
  active boolean DEFAULT true,
  CONSTRAINT basic_workstation_pkey PRIMARY KEY (id),
  CONSTRAINT workstation_workstationtype_fkey FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) DEFERRABLE,
  CONSTRAINT workstation_divisiontion_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE,
  CONSTRAINT workstation_operation_fkey FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) DEFERRABLE
);

-- end


-- Table: jointable_division_workstationtype
-- changed: 22.11.2013

CREATE TABLE jointable_division_workstationtype
(
  workstationtype_id bigint NOT NULL,
  division_id bigint NOT NULL,
  CONSTRAINT jointable_division_workstationtype_pkey PRIMARY KEY (division_id, workstationtype_id),
  CONSTRAINT division_workstationtype_fkey FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) DEFERRABLE,
  CONSTRAINT workstationtype_division_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE
);

-- end


-- Table: jointable_operation_workstation
-- changed: 27.11.2013

CREATE TABLE jointable_operation_workstation
(
  workstation_id bigint NOT NULL,
  operation_id bigint NOT NULL,
  CONSTRAINT jointable_operation_workstation_pkey PRIMARY KEY (operation_id, workstation_id),
  CONSTRAINT operation_workstation_fkey FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE,
  CONSTRAINT workstation_operation_fkey FOREIGN KEY (operation_id)
      REFERENCES technologies_operation (id) DEFERRABLE
);

-- end

-- Table: technologies_operation
-- changed: 05.03.2013

ALTER TABLE technologies_operation ADD COLUMN assignedtooperation character varying(255);
ALTER TABLE technologies_operation ALTER COLUMN assignedtooperation SET DEFAULT '02workstationTypes'::character varying;

ALTER TABLE technologies_operation ADD COLUMN workstationtype_id bigint;

ALTER TABLE technologies_operation ADD COLUMN quantityofworkstations integer;
ALTER TABLE technologies_operation ALTER COLUMN quantityofworkstations SET DEFAULT 1;

-- end

-- Table: technologies_technologyoperationcomponent

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN assignedtooperation character varying(255);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN assignedtooperation SET DEFAULT '02workstationTypes'::character varying;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN workstationtype_id bigint;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN quantityofworkstations integer;
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN quantityofworkstations SET DEFAULT 1;

-- end

-- Table: jointable_technologyoperationcomponent_workstation

CREATE TABLE jointable_technologyoperationcomponent_workstation
(
  technologyoperationcomponent_id bigint NOT NULL,
  workstation_id bigint NOT NULL,
  CONSTRAINT jointable_technologyoperationcomponent_workstation_pkey PRIMARY KEY (workstation_id, technologyoperationcomponent_id),
  CONSTRAINT workstation_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
      REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE,
  CONSTRAINT basic_workstation FOREIGN KEY (workstation_id)
      REFERENCES basic_workstation (id) DEFERRABLE
);

-- end