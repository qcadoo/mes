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
  active boolean DEFAULT true,
  CONSTRAINT basic_workstation_pkey PRIMARY KEY (id),
  CONSTRAINT basic_workstationtype_fkey FOREIGN KEY (workstationtype_id)
      REFERENCES basic_workstationtype (id) DEFERRABLE,
  CONSTRAINT basic_divisiontion_fkey FOREIGN KEY (division_id)
      REFERENCES basic_division (id) DEFERRABLE
);

-- end

-- Table: basic_workstation
-- changed: 22.11.2013
CREATE TABLE jointable_company_negotiation
(
  negotiation_id bigint NOT NULL,
  company_id bigint NOT NULL,
  CONSTRAINT jointable_company_negotiation_pkey PRIMARY KEY (company_id, negotiation_id),
  CONSTRAINT basic_company_fkey FOREIGN KEY (company_id)
      REFERENCES basic_company (id) DEFERRABLE,
  CONSTRAINT supplynegotiations_negotiation_fkey FOREIGN KEY (negotiation_id)
      REFERENCES supplynegotiations_negotiation (id) DEFERRABLE
);
--end