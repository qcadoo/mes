-- Table: basic_division
-- changed: 21.11.2013

ALTER TABLE basic_division ADD COLUMN componentslocation_id BIGINT;
ALTER TABLE basic_division
	ADD CONSTRAINT division_componentslocation_fkey FOREIGN KEY (componentslocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE basic_division ADD COLUMN componentsoutputlocation_id BIGINT;
ALTER TABLE basic_division
	ADD CONSTRAINT division_componentsoutputlocation_fkey FOREIGN KEY (componentsoutputlocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE basic_division ADD COLUMN productsinputlocation_id BIGINT;
ALTER TABLE basic_division
	ADD CONSTRAINT division_productsinputlocation_fkey FOREIGN KEY (productsinputlocation_id)
		REFERENCES materialflow_location (id);

-- end


-- Table: basic_workstation
-- changed: 22.11.2013

CREATE TABLE basic_workstation
(
  id                 BIGINT NOT NULL,
  "number"           CHARACTER VARYING(255),
  name               CHARACTER VARYING(1024),
  description        CHARACTER VARYING(2048),
  workstationtype_id BIGINT,
  division_id        BIGINT,
  operation_id       BIGINT,
  active             BOOLEAN DEFAULT TRUE,
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
  workstationtype_id BIGINT NOT NULL,
  division_id        BIGINT NOT NULL,
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
  workstation_id BIGINT NOT NULL,
  operation_id   BIGINT NOT NULL,
  CONSTRAINT jointable_operation_workstation_pkey PRIMARY KEY (operation_id, workstation_id),
  CONSTRAINT operation_workstation_fkey FOREIGN KEY (workstation_id)
  REFERENCES basic_workstation (id) DEFERRABLE,
  CONSTRAINT workstation_operation_fkey FOREIGN KEY (operation_id)
  REFERENCES technologies_operation (id) DEFERRABLE
);

-- end


-- Table: technologies_operation
-- changed: 05.03.2013

ALTER TABLE technologies_operation ADD COLUMN assignedtooperation CHARACTER VARYING(255);
ALTER TABLE technologies_operation ALTER COLUMN assignedtooperation SET DEFAULT '02workstationTypes' :: CHARACTER VARYING;

-- ALTER TABLE technologies_operation ADD COLUMN workstationtype_id bigint;

ALTER TABLE technologies_operation ADD COLUMN quantityofworkstations INTEGER;
ALTER TABLE technologies_operation ALTER COLUMN quantityofworkstations SET DEFAULT 1;

-- end


-- Table: technologies_technologyoperationcomponent
-- changed: ...

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN assignedtooperation CHARACTER VARYING(255);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN assignedtooperation SET DEFAULT '02workstationTypes' :: CHARACTER VARYING;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN workstationtype_id BIGINT;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN quantityofworkstations INTEGER;
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN quantityofworkstations SET DEFAULT 1;

-- end


-- Table: jointable_technologyoperationcomponent_workstation
-- changed: ...

CREATE TABLE jointable_technologyoperationcomponent_workstation
(
  technologyoperationcomponent_id BIGINT NOT NULL,
  workstation_id                  BIGINT NOT NULL,
  CONSTRAINT jointable_technologyoperationcomponent_workstation_pkey PRIMARY KEY (workstation_id, technologyoperationcomponent_id),
  CONSTRAINT workstation_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
  REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE,
  CONSTRAINT basic_workstation FOREIGN KEY (workstation_id)
  REFERENCES basic_workstation (id) DEFERRABLE
);

-- end


-- Table: operationaltasks_operationaltask
-- changed: 02.05.2014
-- description: for each operational task of type 'other task' replaces empty or blank name with number. See #QCADOOMES-2212

UPDATE operationaltasks_operationaltask SET name = number WHERE typetask = '01otherCase' AND (name IS NULL OR trim(name) = '');

-- end


-- Table: technologies_technology
-- changed: 02.08.2014

ALTER TABLE technologies_technology ADD COLUMN range character varying(255);
ALTER TABLE technologies_technology ALTER COLUMN range SET DEFAULT '01oneDivision'::character varying;

ALTER TABLE technologies_technology ADD COLUMN division_id bigint;
ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_division_fkey FOREIGN KEY (division_id)
		REFERENCES basic_division (id);

ALTER TABLE technologies_technology ADD COLUMN componentslocation_id bigint;
ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_componentslocation_fkey FOREIGN KEY (componentslocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE technologies_technology ADD COLUMN componentsoutputlocation_id bigint;
ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_componentsoutputlocation_fkey FOREIGN KEY (componentsoutputlocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE technologies_technology ADD COLUMN productsinputlocation_id bigint;
ALTER TABLE technologies_technology
	ADD CONSTRAINT technology_productsinputlocation_fkey FOREIGN KEY (productsinputlocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE technologies_technology ADD COLUMN isdivisionlocation boolean;
ALTER TABLE technologies_technology ADD COLUMN isdivisioninputlocation boolean;
ALTER TABLE technologies_technology ADD COLUMN isdivisionoutputlocation boolean;

-- end


-- Table: technologies_operationproductincomponent
-- changed: 02.08.2014

ALTER TABLE technologies_operationproductincomponent ADD COLUMN componentslocation_id bigint;
ALTER TABLE technologies_operationproductincomponent
	ADD CONSTRAINT technology_componentslocation_fkey FOREIGN KEY (componentslocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE technologies_operationproductincomponent ADD COLUMN componentsoutputlocation_id bigint;
ALTER TABLE technologies_operationproductincomponent
	ADD CONSTRAINT technology_componentsoutputlocation_fkey FOREIGN KEY (componentsoutputlocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE technologies_operationproductincomponent ADD COLUMN isdivisionlocation boolean;
ALTER TABLE technologies_operationproductincomponent ADD COLUMN isdivisionoutputlocation boolean;

ALTER TABLE technologies_operationproductincomponent ADD COLUMN flowtypeincomponent character varying(255);
ALTER TABLE technologies_operationproductincomponent ALTER COLUMN flowtypeincomponent SET DEFAULT '01withinTheProcess'::character varying;

-- end


-- Table: technologies_operationproductoutcomponent
-- changed: 02.08.2014

ALTER TABLE technologies_operationproductoutcomponent ADD COLUMN productsinputlocation_id bigint;
ALTER TABLE technologies_operationproductoutcomponent
	ADD CONSTRAINT technology_productsinputlocation_fkey FOREIGN KEY (productsinputlocation_id)
		REFERENCES materialflow_location (id);

ALTER TABLE technologies_operationproductoutcomponent ADD COLUMN isdivisioninputlocation boolean;

ALTER TABLE technologies_operationproductoutcomponent ADD COLUMN flowtypeoutcomponent character varying(255);
ALTER TABLE technologies_operationproductoutcomponent ALTER COLUMN flowtypeoutcomponent SET DEFAULT '01withinTheProcess'::character varying;

-- end


-- Table: basic_parameter
-- changed: 08.05.2014

ALTER TABLE basic_parameter ADD COLUMN workstationsquantityfromproductionline boolean;

-- end


-- Table: technologies_operation
-- changed: 19.05.2014

UPDATE technologies_operation SET assignedtooperation = '01workstations' WHERE workstationtype_id is not null;
UPDATE technologies_operation SET assignedtooperation = '02workstationTypes' WHERE assignedtooperation is null;

-- end


-- Table: technologies_technologyoperationcomponent
-- changed: 19.05.2014

UPDATE technologies_technologyoperationcomponent SET assignedtooperation = '02workstationTypes' WHERE assignedtooperation is null;

UPDATE technologies_technologyoperationcomponent SET quantityofworkstations = 1 WHERE quantityofworkstations is null;

-- end

