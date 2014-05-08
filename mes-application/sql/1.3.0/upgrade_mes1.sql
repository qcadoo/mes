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

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN assignedtooperation CHARACTER VARYING(255);
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN assignedtooperation SET DEFAULT '02workstationTypes' :: CHARACTER VARYING;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN workstationtype_id BIGINT;

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN quantityofworkstations INTEGER;
ALTER TABLE technologies_technologyoperationcomponent ALTER COLUMN quantityofworkstations SET DEFAULT 1;

-- end

-- Table: jointable_technologyoperationcomponent_workstation

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



-- Table: orders_order
-- changed: 20.05.2013

ALTER TABLE orders_order ADD COLUMN ordertype CHARACTER VARYING(255) DEFAULT '01withPatternTechnology' :: CHARACTER VARYING;

-- end


-- Table: technologies_technology
-- changed: 05.06.2013

ALTER TABLE technologies_technology ADD COLUMN technologytype CHARACTER VARYING(255);

-- end


-- Table: technologies_technology
-- changed: 05.06.2013

ALTER TABLE technologies_technology ADD COLUMN technologyprototype_id BIGINT;

ALTER TABLE technologies_technology
ADD CONSTRAINT technology_technology_fkey FOREIGN KEY (technologyprototype_id)
REFERENCES technologies_technology (id) DEFERRABLE;

-- end


-- Table: orders_order
-- changed: 05.06.2013

ALTER TABLE orders_order ADD COLUMN technologyprototype_id BIGINT;

ALTER TABLE orders_order
ADD CONSTRAINT order_technology_fkey FOREIGN KEY (technologyprototype_id)
REFERENCES technologies_technology (id) DEFERRABLE;

-- end


-- Table: basicproductioncounting_productioncountingquantity
-- changed: 04.06.2013

ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE basicproductioncounting_productioncountingquantity
ADD CONSTRAINT productioncountingquantity_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;


ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN basicproductioncounting_id BIGINT;

ALTER TABLE basicproductioncounting_productioncountingquantity
ADD CONSTRAINT productioncountingquantity_basicproductioncounting_fkey FOREIGN KEY (basicproductioncounting_id)
REFERENCES basicproductioncounting_basicproductioncounting (id) DEFERRABLE;

ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN typeofmaterial CHARACTER VARYING(255);
ALTER TABLE basicproductioncounting_productioncountingquantity ALTER COLUMN typeofmaterial SET DEFAULT '01component' :: CHARACTER VARYING;

ALTER TABLE basicproductioncounting_productioncountingquantity ADD COLUMN role CHARACTER VARYING(255);
ALTER TABLE basicproductioncounting_productioncountingquantity ALTER COLUMN role SET DEFAULT '01used' :: CHARACTER VARYING;

-- end


-- Table: technologies_technologyoperationcomponent
-- changed: 22.06.2013

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN createdate TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN updatedate TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN createuser CHARACTER VARYING(255);
ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN updateuser CHARACTER VARYING(255);


-- Table: productioncounting_productionbalance
-- changed: 26.06.2013

ALTER TABLE productioncounting_productionbalance RENAME COLUMN recordsnumber TO trackingsnumber;


-- end


-- Table: timenormsforoperations_techopercomptimecalculation
-- changed: 06.02.2014

CREATE TABLE timenormsforoperations_techopercomptimecalculation
(
  id                                BIGINT NOT NULL,
  operationoffset                   INTEGER,
  effectiveoperationrealizationtime INTEGER,
  effectivedatefrom                 TIMESTAMP WITHOUT TIME ZONE,
  effectivedateto                   TIMESTAMP WITHOUT TIME ZONE,
  duration                          INTEGER DEFAULT 0,
  machineworktime                   INTEGER DEFAULT 0,
  laborworktime                     INTEGER DEFAULT 0,
  CONSTRAINT timenormsforoperations_techopercomptimecalculation_pkey PRIMARY KEY (id)
);

-- end

-- Table: productioncounting_productionrecord
-- changed: 26.06.2013

ALTER TABLE productioncounting_productionrecord RENAME TO productioncounting_productiontracking;

ALTER TABLE productioncounting_productiontracking RENAME COLUMN lastrecord TO lasttracking;

ALTER TABLE productioncounting_productiontracking ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE productioncounting_productiontracking
ADD CONSTRAINT productiontracking_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;

-- end


-- Table: productioncounting_productionrecordstatechange
-- changed: 26.06.2013

ALTER TABLE productioncounting_productionrecordstatechange RENAME TO productioncounting_productiontrackingstatechange;

ALTER TABLE productioncounting_productiontrackingstatechange RENAME COLUMN productionrecord_id TO productiontracking_id;

-- end


-- Table: operationaltasksfororders_techopercompoperationaltask
-- changed: 06.02.2014
CREATE TABLE operationaltasksfororders_techopercompoperationaltask
(
  id                              BIGINT NOT NULL,
  technologyoperationcomponent_id BIGINT,
  CONSTRAINT operationaltasksfororders_techopercompoperationaltask_pkey PRIMARY KEY (id),
  CONSTRAINT techopercompoperationaltask_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
  REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE
);


ALTER TABLE operationaltasks_operationaltask ADD COLUMN techopercompoperationaltask_id BIGINT;

ALTER TABLE operationaltasks_operationaltask
ADD CONSTRAINT operationaltask_techopercompoperationaltask_fkey FOREIGN KEY (techopercompoperationaltask_id)
REFERENCES operationaltasksfororders_techopercompoperationaltask (id) DEFERRABLE;

-- end

-- Table: productioncounting_productioncounting
-- changed: 26.06.2013

ALTER TABLE productioncounting_productioncounting RENAME TO productioncounting_productiontrackingreport;

-- end


-- Table: technologies_technologyoperationcomponent
-- changed: 06.02.2014

ALTER TABLE technologies_technologyoperationcomponent ADD COLUMN techopercomptimecalculation_id BIGINT;

ALTER TABLE technologies_technologyoperationcomponent
ADD CONSTRAINT technologyoperationcomponent_techopercomptimecalculation_fkey FOREIGN KEY (techopercomptimecalculation_id)
REFERENCES timenormsforoperations_techopercomptimecalculation (id) DEFERRABLE;

-- end



-- Table: productioncounting_recordoperationproductincomponent
-- changed: 26.06.2013

ALTER TABLE productioncounting_recordoperationproductincomponent RENAME TO productioncounting_trackingoperationproductincomponent;
ALTER TABLE productioncounting_trackingoperationproductincomponent RENAME COLUMN productionrecord_id TO productiontracking_id;

-- end


-- Table: productioncounting_recordoperationproductoutcomponent
-- changed: 26.06.2013

ALTER TABLE productioncounting_recordoperationproductoutcomponent RENAME TO productioncounting_trackingoperationproductoutcomponent;
ALTER TABLE productioncounting_trackingoperationproductoutcomponent RENAME COLUMN productionrecord_id TO productiontracking_id;

-- end


-- Table: productioncounting_operationtimecomponent
-- changed: 02.07.2013

ALTER TABLE productioncounting_operationtimecomponent ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE productioncounting_operationtimecomponent
ADD CONSTRAINT operationtimecomponent_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;

-- end


-- Table: productioncounting_operationpieceworkcomponent
-- changed: 02.07.2013

ALTER TABLE productioncounting_operationpieceworkcomponent ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE productioncounting_operationpieceworkcomponent
ADD CONSTRAINT operationpieceworkcomponent_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;

-- end


-- Table: productioncountingwithcosts_operationcostcomponent
-- changed: 02.07.2013

ALTER TABLE productioncountingwithcosts_operationcostcomponent ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE productioncountingwithcosts_operationcostcomponent
ADD CONSTRAINT operationcostcomponent_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;

-- end


-- Table: productioncountingwithcosts_operationpieceworkcostcomponent
-- changed: 02.07.2013

ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE productioncountingwithcosts_operationpieceworkcostcomponent
ADD CONSTRAINT operationpieceworkcostcomponent_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;

-- end


-- Table: productioncountingwithcosts_technologyinstoperproductincomp
-- changed: 02.07.2013

ALTER TABLE productioncountingwithcosts_technologyinstoperproductincomp RENAME TO productioncountingwithcosts_technologyoperationproductincomp;

-- end


-- Table: costnormsforoperation_calculationoperationcomponent
-- changed: 02.07.2013

-- FIXME / TODO what about data actually stored in these columns?
ALTER TABLE costnormsforoperation_calculationoperationcomponent DROP COLUMN tj;
ALTER TABLE costnormsforoperation_calculationoperationcomponent DROP COLUMN tpz;
ALTER TABLE costnormsforoperation_calculationoperationcomponent DROP COLUMN machineutilization;
ALTER TABLE costnormsforoperation_calculationoperationcomponent DROP COLUMN laborutilization;
ALTER TABLE costnormsforoperation_calculationoperationcomponent DROP COLUMN timenextoperation;

-- end


-- Table: productionpershift_progressforday
-- changed: 15.07.2013

ALTER TABLE productionpershift_progressforday ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE productionpershift_progressforday
ADD CONSTRAINT progressforday_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;

-- end


-- Table: qualitycontrols_qualitycontrol
-- changed: 19.07.2013

ALTER TABLE qualitycontrols_qualitycontrol ADD COLUMN technologyoperationcomponent_id BIGINT;

ALTER TABLE qualitycontrols_qualitycontrol
ADD CONSTRAINT qualitycontrol_technologyoperationcomponent_fkey FOREIGN KEY (technologyoperationcomponent_id)
REFERENCES technologies_technologyoperationcomponent (id) DEFERRABLE;

-- end


-- Table: workplans_orderoperationinputcolumn
-- changed: 14.08.2013
-- FIXME what about data?
DROP TABLE workplans_orderoperationinputcolumn;

-- end


-- Table: workplans_orderoperationoutputcolumn
-- changed: 14.08.2013
-- FIXME what about data?
DROP TABLE workplans_orderoperationoutputcolumn;

-- end


-- SC#QCADOOMES-1599 -> PKT-6
-- changed: 16.09.2013 by maku

ALTER TABLE basic_parameter ADD COLUMN locktechnologytree BOOLEAN DEFAULT FALSE;
ALTER TABLE basic_parameter ADD COLUMN lockProductionProgress BOOLEAN DEFAULT FALSE;

-- end


-- Table: basicproductioncounting_productioncountingquantity
-- changed: 16.09.2013
-- FIXME what about data?

ALTER TABLE basicproductioncounting_productioncountingquantity DROP COLUMN operationproductincomponent_id;
ALTER TABLE basicproductioncounting_productioncountingquantity DROP COLUMN operationproductoutcomponent_id;

-- end


-- Table operationalTasks_operationalTask
-- description: for each operational task of type 'other task' replaces empty or blank name with number. See #QCADOOMES-2212
-- changed: 02.05.2014

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
