
ALTER TABLE materialflowresources_resource ADD COLUMN quantityinadditionalunit numeric(14,5);

ALTER TABLE materialflowresources_resource ADD COLUMN additionalcode_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN conversion numeric(12,5);
ALTER TABLE materialflowresources_resource ALTER COLUMN conversion SET DEFAULT 0::numeric;

ALTER TABLE materialflowresources_resource ADD COLUMN palletnumber_id bigint;
ALTER TABLE materialflowresources_resource
  ADD CONSTRAINT resource_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;

ALTER TABLE materialflowresources_resource ADD COLUMN typeofpallet character varying(255);

ALTER TABLE materialflowresources_resource ADD COLUMN givenunit character varying(255);

-- end

-- ESILCO-16
CREATE TABLE materialflowresources_documentpositionparameters
(
  id bigint NOT NULL,
  CONSTRAINT materialflowresources_documentpositionparameters_pkey PRIMARY KEY (id)
);

CREATE TABLE materialflowresources_documentpositionparametersitem
(
  id bigint NOT NULL,
  checked boolean DEFAULT true,
  parameters_id bigint,
  name character varying(255),
  CONSTRAINT materialflowresources_documentpositionparametersitem_pkey PRIMARY KEY (id)
);

ALTER TABLE materialflowresources_documentpositionparametersitem
  ADD CONSTRAINT documentpositionparametersitem_parameters_fkey FOREIGN KEY (parameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;

insert into materialflowresources_documentpositionparameters (id) values (1);
insert into materialflowresources_documentpositionparametersitem (name, parameters_id) values 
	('price', 1),
	('storageLocation', 1),
	('additionalCode', 1),
	('productionDate', 1),
	('expirationDate', 1),
	('pallet', 1),
	('typeOfPallet', 1),
	('batch', 1);

ALTER TABLE basic_parameter ADD COLUMN documentpositionparameters_id bigint;

ALTER TABLE basic_parameter
  ADD CONSTRAINT parammeter_documentpositionparameters_fkey FOREIGN KEY (documentpositionparameters_id)
      REFERENCES materialflowresources_documentpositionparameters (id) DEFERRABLE;
-- end


ALTER TABLE materialflowresources_position ADD additionalcode_id bigint;
ALTER TABLE materialflowresources_position ADD conversion numeric(12,5) DEFAULT 0::numeric;
ALTER TABLE materialflowresources_position ADD palletnumber_id bigint;
ALTER TABLE materialflowresources_position ADD typeofpallet character varying(255);

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_additionalcode_fkey FOREIGN KEY (additionalcode_id)
      REFERENCES basic_additionalcode (id) DEFERRABLE;

ALTER TABLE materialflowresources_position
  ADD CONSTRAINT position_palletnumber_fkey FOREIGN KEY (palletnumber_id)
      REFERENCES basic_palletnumber (id) DEFERRABLE;

-- resource lookup changes
-- last touched 23.02.2016 by pako

insert into materialflowresources_documentpositionparametersitem (name, parameters_id) values 
	('resource', 1);

ALTER TABLE materialflowresources_documentpositionparameters ADD COLUMN suggestresource boolean;
ALTER TABLE materialflowresources_documentpositionparameters ALTER COLUMN suggestresource SET DEFAULT true;
-- end